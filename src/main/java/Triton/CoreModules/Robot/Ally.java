package Triton.CoreModules.Robot;

import Proto.RemoteAPI;
import Triton.App;
import Triton.Config.ObjectConfig;
import Triton.Config.PathfinderConfig;
import Triton.CoreModules.AI.Algorithms.PathFinder.JumpPointSearch.JPSPathFinder;
import Triton.CoreModules.AI.Algorithms.PathFinder.PathFinder;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.RobotSockets.RobotConnection;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.Misc.Math.Geometry.Circle2D;
import Triton.Misc.ModulePubSubSystem.*;
import Triton.PeriphModules.Detection.BallData;
import Triton.PeriphModules.Detection.RobotData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

import static Triton.Config.AIConfig.HOLDING_BALL_VEL_THRESH;
import static Triton.Config.ObjectConfig.*;
import static Triton.Config.PathfinderConfig.SPRINT_TO_ROTATE_DIST_THRESH;
import static Triton.CoreModules.Robot.MotionState.*;
import static Triton.CoreModules.Robot.MotionMode.*;
import static Triton.Misc.Math.Coordinates.PerspectiveConverter.calcAngDiff;
import static Triton.Misc.Math.Coordinates.PerspectiveConverter.normAng;

/* TL;DR, Instead, read RobotSkills Interface for a cleaner view !!!!!!! */
public class Ally extends Robot implements AllySkills {
    private final RobotConnection conn;
    /*** external pub sub ***/
    private final Subscriber<HashMap<String, Integer>> fieldSizeSub;
    private final ArrayList<Subscriber<RobotData>> yellowRobotSubs;
    private final ArrayList<Subscriber<RobotData>> blueRobotSubs;
    private final Subscriber<BallData> ballSub;
    private final Subscriber<Boolean> dribStatSub;
    private final Publisher<RemoteAPI.Commands> commandsPub;

    /*** internal pub sub ***/
    private final Publisher<MotionState> statePub;
    private final Subscriber<MotionState> stateSub;
    private final Publisher<Vec2D> pointPub, kickVelPub, holdBallPosPub;
    private final Subscriber<Vec2D> pointSub, kickVelSub, holdBallPosSub;
    private final Publisher<Double> angPub;
    private final Subscriber<Double> angSub;

    protected ThreadPoolExecutor threadPool;
    private PathFinder pathFinder;

    private boolean prevHoldBallStatus = false;

    public Ally(Team team, int ID) {
        super(team, ID);
        this.threadPool = App.threadPool;

        conn = new RobotConnection(ID);

        fieldSizeSub = new FieldSubscriber<>("geometry", "fieldSize");

        blueRobotSubs = new ArrayList<>();
        yellowRobotSubs = new ArrayList<>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            blueRobotSubs.add(new FieldSubscriber<>("detection", Team.BLUE.name() + i));
            yellowRobotSubs.add(new FieldSubscriber<>("detection", Team.YELLOW.name() + i));
        }
        ballSub = new FieldSubscriber<>("detection", "ball");

        statePub = new FieldPublisher<>("Ally state", "" + ID, MOVE_TVRV);
        pointPub = new FieldPublisher<>("Ally point", "" + ID, new Vec2D(0, 0));
        angPub = new FieldPublisher<>("Ally ang", "" + ID, 0.0);
        kickVelPub = new FieldPublisher<>("Ally kickVel", "" + ID, new Vec2D(0, 0));
        holdBallPosPub = new FieldPublisher<>("Ally holdBallPos", "" + ID, null);

        stateSub = new FieldSubscriber<>("Ally state", "" + ID);
        pointSub = new FieldSubscriber<>("Ally point", "" + ID);
        angSub = new FieldSubscriber<>("Ally ang", "" + ID);
        kickVelSub = new FieldSubscriber<>("Ally kickVel", "" + ID);
        holdBallPosSub = new FieldSubscriber<>("Ally holdBallPos", "" + ID);

        dribStatSub = new FieldSubscriber<>("Ally drib", "" + ID);
        commandsPub = new MQPublisher<>("commands", "" + ID);

        conn.buildTcpConnection();
        conn.buildCommandUDP();
        // conn.buildDataStream(port + ConnectionConfig.DATA_UDP_OFFSET);
        conn.buildVisionStream(team);
    }

    public boolean connect() {
        boolean rtn = false;
        try {
            rtn = conn.getTCPConnection().connect();
        } catch (IOException e) {
            // System.out.printf("Robot %d TCP connection fails: %s\n", super.ID, e.getClass());
            e.printStackTrace();
        }
        return rtn;
    }

    public void reinit() {
        // To-do for physical robots
    }

    /*** primitive control methods ***/
    public void autoCap() {
        statePub.publish(AUTO_CAPTURE);
    }

    @Override
    public void stop() {
        moveAt(new Vec2D(0, 0));
        spinAt(0);
    }

    /**
     * @param loc player perspective, millimeter
     */
    @Override
    public void moveTo(Vec2D loc) {
        switch (stateSub.getMsg()) {
            case MOVE_TDRD, MOVE_TVRD, MOVE_NSTDRD -> statePub.publish(MOVE_TDRD);
            default -> statePub.publish(MOVE_TDRV);
        }
        pointPub.publish(loc);
    }

    /**
     * @param vel player perspective, vector with unit as percentage from -100 to 100
     */
    @Override
    public void moveAt(Vec2D vel) {
        switch (stateSub.getMsg()) {
            case MOVE_TDRD, MOVE_TVRD, MOVE_NSTDRD -> statePub.publish(MOVE_TVRD);
            default -> statePub.publish(MOVE_TVRV);
        }
        pointPub.publish(vel);
    }

    /**
     * @param angle player perspective, degrees, starting from y-axis, positive is counter clockwise
     */
    @Override
    public void spinTo(double angle) {
        switch (stateSub.getMsg()) {
            case MOVE_TDRD, MOVE_TDRV, MOVE_NSTDRD, MOVE_NSTDRV -> statePub.publish(MOVE_TDRD);
            default -> statePub.publish(MOVE_TVRD);
        }
        angPub.publish(angle);
    }

    /**
     * @param angVel unit is percentage from -100 to 100, positive is counter clockwise
     */
    @Override
    public void spinAt(double angVel) {
        switch (stateSub.getMsg()) {
            case MOVE_TDRD, MOVE_TDRV, MOVE_NSTDRD, MOVE_NSTDRV -> statePub.publish(MOVE_TDRV);
            default -> statePub.publish(MOVE_TVRV);
        }
        angPub.publish(angVel);
    }

    // runs in the caller thread
    @Override
    public void kick(Vec2D kickVel) {
        double mag = kickVel.mag();
        if (mag >= MAX_KICK_VEL) {
            kickVel = kickVel.norm().scale(MAX_KICK_VEL);
        }

        kickVelPub.publish(kickVel);

        threadPool.submit(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            kickVelPub.publish(new Vec2D(0, 0));
        });
    }



    private void moveToNoSlowDown(Vec2D loc) {
        switch (stateSub.getMsg()) {
            case MOVE_TDRD, MOVE_TVRD, MOVE_NSTDRD -> statePub.publish(MOVE_NSTDRD);
            default -> statePub.publish(MOVE_NSTDRV);
        }
        pointPub.publish(loc);
    }


    /*** advanced control methods with path avoiding obstacles ***/

    // Note: (moveTo/At & spinTo/At] are mutually exclusive to advanced control methods

    @Override
    public void rotateTo(double angle) {
        double targetAngle = normAng(angle);
        double angDiff = calcAngDiff(targetAngle, getDir());

        double absAngleDiff = Math.abs(angDiff);
        if (absAngleDiff <= PathfinderConfig.RD_ANGLE_THRESH) {
            moveAt(new Vec2D(0, 0));
            spinTo(targetAngle);
        } else {
            moveAt(new Vec2D(0, 0));
            spinAt((isHoldingBall()) ? Math.signum(angDiff) * HOLDING_BALL_VEL_THRESH : Math.signum(angDiff) * 100);
        }
    }

    @Override
    public void strafeTo(Vec2D endPoint) {
        strafeTo(endPoint, getDir());
    }

    @Override
    public void strafeTo(Vec2D endPoint, double angle) {
        double targetAngle = normAng(angle);
        double angDiff = calcAngDiff(targetAngle, getDir());
        double absAngleDiff = Math.abs(angDiff);

        if (absAngleDiff <= PathfinderConfig.RD_ANGLE_THRESH) {
            spinTo(targetAngle);
        } else {
            spinAt((isHoldingBall()) ? Math.signum(angDiff) * HOLDING_BALL_VEL_THRESH : Math.signum(angDiff) * 100);
        }

        if (absAngleDiff <= PathfinderConfig.MOVE_ANGLE_THRESH) {
            ArrayList<Vec2D> path = findPath(endPoint);
            if (path != null && path.size() > 0) {
                Vec2D nextNode;
                if (path.size() == 1) nextNode = path.get(0);
                else if (path.size() == 2) nextNode = path.get(1);
                else nextNode = path.get(1);

                if (path.size() <= 2) moveTo(nextNode);
                else moveToNoSlowDown(nextNode);
            } else {
                moveAt(new Vec2D(0,0));
            }
        } else {
            moveAt(new Vec2D(0, 0));
        }
    }

    @Override
    public void curveTo(Vec2D endPoint) {
        curveTo(endPoint, getDir());
    }

    @Override
    public void curveTo(Vec2D endPoint, double angle) {
        double targetAngle = normAng(angle);
        double angDiff = calcAngDiff(targetAngle, getDir());
        double absAngleDiff = Math.abs(angDiff);

        spinTo(targetAngle);

        ArrayList<Vec2D> path = findPath(endPoint);
        if (path != null && path.size() > 0) {
            Vec2D nextNode;
            if (path.size() == 1) nextNode = path.get(0);
            else if (path.size() == 2) nextNode = path.get(1);
            else nextNode = path.get(1);

            if (path.size() <= 2) moveTo(nextNode);
            else moveToNoSlowDown(nextNode);
        } else {
            moveAt(new Vec2D(0,0));
        }
    }

    @Override
    public void fastCurveTo(Vec2D endPoint) {
        fastCurveTo(endPoint, getDir());
    }

    @Override
    public void fastCurveTo(Vec2D endPoint, double endAngle) {
        // included rear-prioritizing case
        ArrayList<Vec2D> path = findPath(endPoint);
        if (path != null && path.size() > 0) {
            double fastestAngle = endPoint.sub(getPos()).toPlayerAngle();
            Vec2D nextNode;
            if (path.size() == 1) nextNode = path.get(0);
            else if (path.size() == 2) nextNode = path.get(1);
            else nextNode = path.get(1);

            double angDiff = calcAngDiff(fastestAngle, getDir());
            double absAngleDiff = Math.abs(angDiff);

            if(endPoint.sub(getPos()).mag() < SPRINT_TO_ROTATE_DIST_THRESH) {
                spinTo(endAngle);
            }
            else {
                if (absAngleDiff <= 90) spinTo(fastestAngle);
                else spinTo(normAng(fastestAngle + 180));
            }
            if (path.size() <= 2) moveTo(nextNode);
            else moveToNoSlowDown(nextNode);
        } else {
            moveAt(new Vec2D(0,0));
            spinAt(0);
        }
    }

    @Override
    public void sprintFrontTo(Vec2D endPoint) {
        sprintFrontTo(endPoint, getDir());
    }

    @Override
    public void sprintFrontTo(Vec2D endPoint, double endAngle) {
        ArrayList<Vec2D> path = findPath(endPoint);
        if (path != null && path.size() > 0) {
            double fastestAngle = 0;
            boolean fastestAngleFound = false;
            for (Vec2D node : path) {
                double dist = node.sub(getPos()).mag();
                if (dist >= SPRINT_TO_ROTATE_DIST_THRESH) {
                    fastestAngle = node.sub(getPos()).toPlayerAngle();
                    fastestAngleFound = true;
                    break;
                }
            }

            Vec2D nextNode;
            if (path.size() == 1) nextNode = path.get(0);
            else if (path.size() == 2) nextNode = path.get(1);
            else nextNode = path.get(1);

            if (!fastestAngleFound) fastestAngle = endAngle;

            double angDiff = calcAngDiff(fastestAngle, getDir());
            double absAngleDiff = Math.abs(angDiff);

            if(absAngleDiff <= PathfinderConfig.RD_ANGLE_THRESH) {
                spinTo(fastestAngle);
            } else {
                spinAt((isHoldingBall()) ? Math.signum(angDiff) * HOLDING_BALL_VEL_THRESH : Math.signum(angDiff) * 60);
            }

            if (absAngleDiff <= PathfinderConfig.MOVE_ANGLE_THRESH) {
                if (path.size() <= 2) moveTo(nextNode);
                else moveToNoSlowDown(nextNode);
            } else {
                moveAt(new Vec2D(0, 0));
            }
        } else {
            moveAt(new Vec2D(0,0));
            spinAt(0);
        }
    }


    @Override
    public void sprintTo(Vec2D endPoint) {
        sprintTo(endPoint, getDir());
    }

    @Override
    public void sprintTo(Vec2D endPoint, double endAngle) {
        // included rear-prioritizing case
        ArrayList<Vec2D> path = findPath(endPoint);
        if (path != null && path.size() > 0) {
            double fastestAngle = 0;
            boolean fastestAngleFound = false;
            for (Vec2D node : path) {
                double dist = node.sub(getPos()).mag();
                if (dist >= SPRINT_TO_ROTATE_DIST_THRESH) {
                    fastestAngle = node.sub(getPos()).toPlayerAngle();
                    fastestAngleFound = true;
                    break;
                }
            }

            Vec2D nextNode;
            if (path.size() == 1) nextNode = path.get(0);
            else if (path.size() == 2) nextNode = path.get(1);
            else nextNode = path.get(1);

            if (!fastestAngleFound) fastestAngle = endAngle;

            double angDiff = calcAngDiff(fastestAngle, getDir());
            double absAngleDiff = Math.abs(angDiff);

            if(absAngleDiff <= 90) {
                spinTo(fastestAngle);
                if (absAngleDiff <= PathfinderConfig.MOVE_ANGLE_THRESH) {
                    if (path.size() <= 2) moveTo(nextNode);
                    else moveToNoSlowDown(nextNode);
                } else {
                    moveAt(new Vec2D(0, 0));
                }
            }
            else {
                if (!fastestAngleFound) spinTo(normAng(fastestAngle));
                else spinTo(normAng(fastestAngle + 180));

                if (absAngleDiff >= 180 - PathfinderConfig.MOVE_ANGLE_THRESH) {
                    if (path.size() <= 2) moveTo(nextNode);
                    else moveToNoSlowDown(nextNode);
                } else {
                    moveAt(new Vec2D(0, 0));
                }
            }
        } else {
            moveAt(new Vec2D(0,0));
            spinAt(0);
        }
    }

    /*** Soccer Skills methods ***/
    @Override
    public void getBall(Ball ball) {
        Vec2D ballLoc = ball.getPos();
        Vec2D currPos = getPos();
        Vec2D currPosToBall = ballLoc.sub(currPos);
        if (currPosToBall.mag() <= PathfinderConfig.AUTOCAP_DIST_THRESH) {
            statePub.publish(AUTO_CAPTURE);
        } else {
            /* To-do: once intercept is ready, use it with sprintTo for 180 degree situation */
            sprintFrontTo(ballLoc, currPosToBall.toPlayerAngle());
        }
    }

    @Override
    public void dribRotate(Ball ball, double angle) {
        dribRotate(ball, angle, 0);
    }

    @Override
    public void dribRotate(Ball ball, double angle, double offsetDist) {
        Vec2D allyToBall = ball.getPos().sub(getPos());
        double allyToBallDist = allyToBall.mag();

        Vec2D angleUnitVec = new Vec2D(Math.cos(Math.toRadians(angle + 90)), Math.sin(Math.toRadians(angle + 90)));
        Vec2D angleOffsetVec = angleUnitVec.scale(allyToBallDist + offsetDist);
        Vec2D targetPos = ball.getPos().sub(angleOffsetVec);
        curveTo(targetPos, angle);
    }

    @Override
    public void passBall(Vec2D receivePos, double ETA) {
        Vec2D allyToReceivePos = receivePos.sub(getPos());
        double distToTarget = allyToReceivePos.mag();
        double targetBallVel = (distToTarget / ETA) / 1000.0;
        System.out.println(targetBallVel);
        kick(new Vec2D(targetBallVel, 2));
    }

    @Override
    public void receive(Ball ball, Vec2D anchorPos) {
        Vec2D currPos = getPos();
        Vec2D ballPos = ball.getPos();
        Vec2D ballVelDir = ball.getVel().norm();
        Vec2D ballToAnchor = anchorPos.sub(ballPos);
        Vec2D receivePoint = ballPos.add(ballVelDir.scale(ballToAnchor.dot(ballVelDir)));
        // Vec2 receiveHoriVec = (Vec2) Mat2.rotation(90).mult(ballVelDir);

        if(currPos.sub(ballPos).mag() < PathfinderConfig.AUTOCAP_DIST_THRESH) {
            getBall(ball);
        }
        else {
            // To-do: if(ball.getVel().mag() < )
            strafeTo(receivePoint, normAng(ballVelDir.toPlayerAngle() + 180));
        }
    }

    @Override
    public void intercept(Ball ball) {

        /* To-do */

        /* ad hoc dealing, will upgrade it later */
        getBall(ball);
    }

    @Override
    public boolean isHoldingBall() {
        if (!dribStatSub.isSubscribed()) {
            return false;
        }
        return dribStatSub.getMsg();
    }

    @Override
    public double dispSinceHoldBall() {
        Vec2D holdBallPos = holdBallPosSub.getMsg();

        if (holdBallPos != null)
            return getPos().sub(holdBallPos).mag();
        else
            return 0;
    }

    @Override
    public boolean isMaxDispExceeded() {
        return dispSinceHoldBall() > EXCESSIVE_DRIBBLING_DIST;
    }

    @Override
    public boolean isPosArrived(Vec2D pos) {
        return pos.sub(getPos()).mag() < POS_PRECISION;
    }

    @Override
    public boolean isDirAimed(double angle) {
        return Math.abs(calcAngDiff(angle, getDir())) < DIR_PRECISION;
    }

    // Everything in run() runs in the Ally Thread
    @Override
    public void run() {
        try {
            subscribe();
            super.run();
            initPathfinder();

            threadPool.submit(conn.getTCPConnection());
            threadPool.submit(conn.getTCPConnection().getSendTCP());
            threadPool.submit(conn.getTCPConnection().getReceiveTCP());
            threadPool.submit(conn.getCommandStream());
            threadPool.submit(conn.getVisionStream());

            conn.getTCPConnection().sendInit();

            while (true) { // delay added
                RemoteAPI.Commands command;
                MotionState state = stateSub.getMsg();

                switch (state) {
                    case MOVE_TDRD -> command = createTDRDCmd();
                    case MOVE_TDRV -> command = createTDRVCmd();
                    case MOVE_TVRD -> command = createTVRDCmd();
                    case MOVE_TVRV -> command = createTVRVCmd();
                    case MOVE_NSTDRD -> command = createNSTDRDCmd();
                    case MOVE_NSTDRV -> command = createNSTDRVCmd();
                    case AUTO_CAPTURE -> command = createAutoCapCmd();
                    default -> {
                        command = createTVRVCmd();
                    }
                }
                commandsPub.publish(command);

                if (isHoldingBall() != prevHoldBallStatus) {
                    if (isHoldingBall()) {
                        holdBallPosPub.publish(getPos());
                    } else {
                        holdBallPosPub.publish(null);
                    }
                }
                prevHoldBallStatus = isHoldingBall();

                // avoid starving other threads
                Thread.sleep(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void subscribe() {
        super.subscribe();
        try {
            fieldSizeSub.subscribe(1000);
            for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
                yellowRobotSubs.get(i).subscribe(1000);
                blueRobotSubs.get(i).subscribe(1000);
            }
            ballSub.subscribe(1000);

            dribStatSub.subscribe(1000);
            stateSub.subscribe(1000);
            pointSub.subscribe(1000);
            angSub.subscribe(1000);
            kickVelSub.subscribe(1000);
            holdBallPosSub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize pathfinder with geometry information
     */
    private void initPathfinder() {
        while (pathFinder == null) {
            HashMap<String, Integer> fieldSize = fieldSizeSub.getMsg();

            if (fieldSize == null || fieldSize.get("fieldLength") == 0 || fieldSize.get("fieldWidth") == 0)
                continue;

            double worldSizeX = fieldSize.get("fieldWidth");
            double worldSizeY = fieldSize.get("fieldLength");

            pathFinder = new JPSPathFinder(worldSizeX, worldSizeY);
        }
    }

    public void displayPathFinder() {
        if (pathFinder instanceof JPSPathFinder) {
            ((JPSPathFinder) pathFinder).display();
        }
    }

    /**
     * Update the set path of the robot
     */
    private ArrayList<Vec2D> findPath(Vec2D endPoint) {
        pathFinder.setObstacles(getObstacles());
        return pathFinder.findPath(getPos(), endPoint);
    }

    /**
     * Returns an ArrayList of circles representing the obstacles for pathfinding
     *
     * @return an ArrayList of circles representing the obstacles for pathfinding
     */
    private ArrayList<Circle2D> getObstacles() {
        ArrayList<RobotData> blueRobots = new ArrayList<>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            blueRobots.add(blueRobotSubs.get(i).getMsg());
        }

        ArrayList<RobotData> yellowRobots = new ArrayList<>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobots.add(yellowRobotSubs.get(i).getMsg());
        }

        ArrayList<Circle2D> obstacles = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            if (team == Team.YELLOW && ID == i)
                continue;
            RobotData robot = yellowRobots.get(i);
            obstacles.add(new Circle2D(robot.getPos(), ObjectConfig.ROBOT_RADIUS));
        }
        for (int i = 0; i < 6; i++) {
            if (team == Team.BLUE && ID == i)
                continue;
            RobotData robot = blueRobots.get(i);
            obstacles.add(new Circle2D(robot.getPos(), ObjectConfig.ROBOT_RADIUS));
        }

        return obstacles;
    }


    /*** TL;DR ***/

    private RemoteAPI.Commands createPrimitiveCmdBuilder(MotionMode mode) {
        RemoteAPI.Commands.Builder command = RemoteAPI.Commands.newBuilder();
        command.setIsWorldFrame(true);
        command.setEnableBallAutoCapture(false);
        command.setMode(mode.ordinal());

        RemoteAPI.Vec3D.Builder motionSetPoint = RemoteAPI.Vec3D.newBuilder();
        Vec2D point = pointSub.getMsg();
        motionSetPoint.setX(point.x);
        motionSetPoint.setY(point.y);
        double angle = normAng(angSub.getMsg());
        motionSetPoint.setZ(angle);
        command.setMotionSetPoint(motionSetPoint);

        RemoteAPI.Vec2D.Builder kickerSetPoint = RemoteAPI.Vec2D.newBuilder();
        Vec2D kickVel = kickVelSub.getMsg();
        kickerSetPoint.setX(kickVel.x);
        kickerSetPoint.setY(kickVel.y);
        command.setKickerSetPoint(kickerSetPoint);

        return command.build();
    }

    private RemoteAPI.Commands createTDRDCmd() {
        return createPrimitiveCmdBuilder(TDRD);
    }

    private RemoteAPI.Commands createTDRVCmd() {
        return createPrimitiveCmdBuilder(TDRV);
    }

    private RemoteAPI.Commands createTVRDCmd() {
        return createPrimitiveCmdBuilder(TVRD);
    }

    private RemoteAPI.Commands createTVRVCmd() {
        return createPrimitiveCmdBuilder(TVRV);
    }

    private RemoteAPI.Commands createNSTDRDCmd() {
        return createPrimitiveCmdBuilder(NSTDRD);
    }

    private RemoteAPI.Commands createNSTDRVCmd() {
        return createPrimitiveCmdBuilder(NSTDRV);
    }

    private RemoteAPI.Commands createAutoCapCmd() {
        RemoteAPI.Commands.Builder command = RemoteAPI.Commands.newBuilder();
        command.setIsWorldFrame(true);
        command.setEnableBallAutoCapture(true);
        command.setMode(TVRV.ordinal());

        RemoteAPI.Vec3D.Builder motionSetPoint = RemoteAPI.Vec3D.newBuilder();
        Vec2D point = pointSub.getMsg();
        motionSetPoint.setX(point.x);
        motionSetPoint.setY(point.y);
        double angle = normAng(angSub.getMsg());
        motionSetPoint.setZ(angle);
        command.setMotionSetPoint(motionSetPoint);

        RemoteAPI.Vec2D.Builder kickerSetPoint = RemoteAPI.Vec2D.newBuilder();
        Vec2D kickVel = kickVelSub.getMsg();
        kickerSetPoint.setX(kickVel.x);
        kickerSetPoint.setY(kickVel.y);
        command.setKickerSetPoint(kickerSetPoint);

        return command.build();
    }


}