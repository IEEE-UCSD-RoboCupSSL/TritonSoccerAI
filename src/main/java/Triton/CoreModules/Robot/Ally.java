package Triton.CoreModules.Robot;

import Proto.RemoteAPI;
import Triton.App;
import Triton.Config.ObjectConfig;
import Triton.Config.PathfinderConfig;
import Triton.CoreModules.AI.PathFinder.JumpPointSearch.JPSPathFinder;
import Triton.CoreModules.AI.PathFinder.PathFinder;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.RobotSockets.RobotConnection;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.Geometry.Circle2D;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.Misc.ModulePubSubSystem.*;
import Triton.PeriphModules.Detection.BallData;
import Triton.PeriphModules.Detection.RobotData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

import static Triton.Config.AIConfig.HOLDING_BALL_VEL_THRESH;
import static Triton.Config.GeometryConfig.*;
import static Triton.Config.ObjectConfig.*;
import static Triton.Config.PathfinderConfig.*;
import static Triton.CoreModules.Robot.MotionMode.*;
import static Triton.CoreModules.Robot.MotionState.*;
import static Triton.Misc.Math.Coordinates.PerspectiveConverter.calcAngDiff;
import static Triton.Misc.Math.Coordinates.PerspectiveConverter.normAng;

/* TL;DR, Instead, read RobotSkills Interface for a cleaner view !!!!!!! */
public class Ally extends Robot implements AllySkills {
    private final RobotConnection conn;

    /*** external pub sub ***/
    private final ArrayList<Subscriber<RobotData>> yellowRobotSubs;
    private final ArrayList<Subscriber<RobotData>> blueRobotSubs;
    private final Subscriber<BallData> ballSub;
    private final Subscriber<Boolean> dribStatSub;
    private final Publisher<RemoteAPI.CommandData> commandsPub;

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
        conn.buildUDPStream();
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
            kickVel = kickVel.normalized().scale(MAX_KICK_VEL);
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
                moveAt(new Vec2D(0, 0));
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
            moveAt(new Vec2D(0, 0));
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

            if (endPoint.sub(getPos()).mag() < SPRINT_TO_ROTATE_DIST_THRESH) {
                spinTo(endAngle);
            } else {
                if (absAngleDiff <= 90) spinTo(fastestAngle);
                else spinTo(normAng(fastestAngle + 180));
            }
            if (path.size() <= 2) moveTo(nextNode);
            else moveToNoSlowDown(nextNode);
        } else {
            moveAt(new Vec2D(0, 0));
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

            if (absAngleDiff <= PathfinderConfig.RD_ANGLE_THRESH) {
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
            moveAt(new Vec2D(0, 0));
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

            if (absAngleDiff <= 90) {
                spinTo(fastestAngle);
                if (absAngleDiff <= PathfinderConfig.MOVE_ANGLE_THRESH) {
                    if (path.size() <= 2) moveTo(nextNode);
                    else moveToNoSlowDown(nextNode);
                } else {
                    moveAt(new Vec2D(0, 0));
                }
            } else {
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
            moveAt(new Vec2D(0, 0));
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
            fastCurveTo(ballLoc, currPosToBall.toPlayerAngle());
            //dynamicIntercept(ball, 0);
        }
    }

    @Override
    public boolean dribRotate(Ball ball, double angle) {
        return dribRotate(ball, angle, 0);
    }

    @Override
    public boolean dribRotate(Ball ball, double angle, double offsetDist) {
        Vec2D angleUnitVec = new Vec2D(angle);
        Vec2D angleOffsetVec = angleUnitVec.scale(ROBOT_MIN_RADIUS + BALL_RADIUS + offsetDist);
        Vec2D targetPos = ball.getPos().sub(angleOffsetVec);
        curveTo(targetPos, angle);

        if (ball.getPos().sub(getPos()).mag() - ROBOT_MIN_RADIUS - BALL_RADIUS > 30) {
            return false;
        }
        return true;
    }

    @Override
    public void staticIntercept(Ball ball, Vec2D anchorPos) {
        // To-do (future) : edge case: ball going opposite dir

        Vec2D currPos = getPos();
        Vec2D ballPos = ball.getPos();
        Vec2D ballVelDir = ball.getVel().normalized();
        Vec2D ballToAnchor = anchorPos.sub(ballPos);
        Vec2D receivePoint = ballPos.add(ballVelDir.scale(ballToAnchor.dot(ballVelDir)));

        if (currPos.sub(ballPos).mag() < PathfinderConfig.AUTOCAP_DIST_THRESH) {
            getBall(ball);
        } else {
            if (ball.getVel().mag() < 750) { // To-do: magic number && comment vel unit
                getBall(ball);
            } else {
                strafeTo(receivePoint, normAng(ballVelDir.toPlayerAngle() + 180));
            }
        }
    }

    @Override
    public void dynamicIntercept(Ball ball, double faceDir) {
        Vec2D currPos = getPos();
        Vec2D ballPos = ball.getPos();

        if (currPos.sub(ballPos).mag() < PathfinderConfig.AUTOCAP_DIST_THRESH) {
            getBall(ball);
        } else {
            Vec2D faceVec = new Vec2D(faceDir).normalized();
            Vec2D offset = faceVec.scale(DRIBBLER_OFFSET);
            Vec2D targetPos = ball.getPos().sub(offset);
            ArrayList<Vec2D> circCenters = getCircleCenters(ball.getPos(), targetPos,
                        faceVec, offset.mag(), INTERCEPT_CIRCLE_RADIUS);

            // System.out.println(circCenters);

            Vec2D interceptPoint = getInterceptPoint(getPos(), targetPos, faceVec, INTERCEPT_CIRCLE_RADIUS, circCenters);

            //System.out.println(interceptPoint);
            curveTo(interceptPoint, ball.getPos().sub(getPos()).toPlayerAngle());
        }
    }

    @Override
    public void keep(Ball ball, Vec2D aimTraj) {
        Vec2D currPos = getPos();
        Vec2D ballPos = ball.getPos();
        double y = -FIELD_LENGTH / 2 + 150;

        if (currPos.sub(ballPos).mag() < PathfinderConfig.AUTOCAP_DIST_THRESH) {
            getBall(ball);
        } else {
            double x;
            if (Math.abs(aimTraj.y) <= 0.0001 || Math.abs(aimTraj.x) <= 0.0001) {
                x = ballPos.x;
            } else {
                double m = aimTraj.y / aimTraj.x;
                double b = ballPos.y - (ballPos.x * m);
                x = (y - b) / m;
            }

            x = Math.max(x, GOAL_LEFT);
            x = Math.min(x, GOAL_RIGHT);
            Vec2D targetPos = new Vec2D(x, y);
            fastCurveTo(targetPos);
        }
    }

    @Override
    public void receive(Ball ball, Vec2D receivePos) {
        // To-do: devise new implementation or choose the better between the two intercept impl
        staticIntercept(ball, receivePos);
    }

    @Override
    public boolean isHoldingBall() {
        if (!dribStatSub.isSubscribed()) {
            return false;
        }
        return dribStatSub.getMsg();
    }

    @Override
    public Vec2D HoldBallPos() {
        return  holdBallPosSub.getMsg();
    }


    @Override
    public boolean isPosArrived(Vec2D pos) {
        return isPosArrived(pos, POS_PRECISION);
    }

    @Override
    public boolean isPosArrived(Vec2D pos, double dist) {
        return pos.sub(getPos()).mag() < dist;
    }

    @Override
    public boolean isDirAimed(double angle) {
        return isDirAimed(angle, DIR_PRECISION);
    }

    @Override
    public boolean isDirAimed(double angle, double angleDiff) {
        return Math.abs(calcAngDiff(angle, getDir())) < angleDiff;
    }

    public ArrayList<Vec2D> getCircleCenters(Vec2D ballPos, Vec2D targetPos, Vec2D faceVec, double ballTargetDist, double circRad) {
        Vec2D circCentersVec = new Vec2D(faceVec.y, -faceVec.x);
        Vec2D ballTargetMidpoint = ballPos.add(targetPos).scale(0.5);
        double centerMidpointDist = Math.pow(circRad * circRad - 1 / 4 * ballTargetDist * ballTargetDist, 0.5);
        ArrayList<Vec2D> circCenters = new ArrayList<>();
        circCenters.add(ballTargetMidpoint.add(circCentersVec.scale(centerMidpointDist)));
        circCenters.add(ballTargetMidpoint.sub(circCentersVec.scale(centerMidpointDist)));
        return circCenters;
    }

    private Vec2D getInterceptPoint(Vec2D allyPos, Vec2D targetPos, Vec2D faceVec, double circRad, ArrayList<Vec2D> circCenters) {
        Vec2D targetToCenter = circCenters.get(0).sub(targetPos);
        // double alpha = Vec2D.angleDiff(faceVec, targetToCenter);
        double alpha = 20;

        Vec2D allyToTarget = targetPos.sub(allyPos);
        double angleDiff = PerspectiveConverter.calcAngDiff(allyToTarget.scale(-1.0).toPlayerAngle(),
                    faceVec.scale(-1.0).toPlayerAngle());

        //System.out.println(angleDiff);

        Vec2D vel = new Vec2D(0, 0);
        if (Math.abs(angleDiff) < alpha) {
            // vel = targetPos.sub(allyPos).normalized();
            vel = allyToTarget;
            // System.out.println("Baga");
        } else {

            // Determine which circle is closer to the robot
            double dist0 = allyPos.sub(circCenters.get(0)).mag();
            double dist1 = allyPos.sub(circCenters.get(1)).mag();
            double botToCenterDist = Math.min(dist0, dist1);
            Vec2D center = dist0 <= dist1 ? circCenters.get(0) : circCenters.get(1);
            double angDir = -1.0;
            if(dist0 < dist1) angDir = 1.0;
            // System.out.println(center);

            Vec2D allyToCenter = center.sub(allyPos).normalized();
            // Robot outside of both circles
            if (isOutsideCircles(circCenters, circRad, allyPos)) {
                // Find the tangent direction and the point of tangency
                double tangentLength = Math.pow(botToCenterDist * botToCenterDist - circRad * circRad, 0.5);

                if(Math.abs(tangentLength) < 0.01) tangentLength = 0.01;
                double tangentAngle = Math.atan2(circRad, tangentLength);

                Vec2D tangentVec = allyToCenter.rotate(Math.toDegrees(angDir * tangentAngle) + angDir * 30);
                vel = tangentVec.scale(tangentLength);
            } else { // Inside of either circle
                vel = center.sub(allyPos).rotate(angDir * Math.toDegrees(65)).scale(1.5);
                //System.out.println("Inside:" + vel);
            }
        }
        //return allyPos.add(vel.scale(1000));

        return allyPos.add(vel);
    }

    private boolean isCloserToFaceVec(Vec2D faceVec, Vec2D targetPos, Vec2D allyPos, double alpha) {
        Vec2D allyToTarget = targetPos.sub(allyPos);
        return Math.abs(Vec2D.angleDiff(faceVec, allyToTarget)) <= alpha;
    }

    private boolean isOutsideCircles(ArrayList<Vec2D> circCenters, double circRad, Vec2D allyPos) {
        return Vec2D.dist(allyPos, circCenters.get(0)) > circRad && Vec2D.dist(allyPos, circCenters.get(1)) > circRad;
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

    private void moveToNoSlowDown(Vec2D loc) {
        switch (stateSub.getMsg()) {
            case MOVE_TDRD, MOVE_TVRD, MOVE_NSTDRD -> statePub.publish(MOVE_NSTDRD);
            default -> statePub.publish(MOVE_NSTDRV);
        }
        pointPub.publish(loc);
    }

    // Everything in run() runs in the Ally Thread
    @Override
    public void run() {
        try {
            subscribe();
            super.run();
            pathFinder = new JPSPathFinder(FIELD_WIDTH, FIELD_LENGTH);

            threadPool.submit(conn.getTCPConnection());
            threadPool.submit(conn.getTCPConnection().getSendTCP());
            threadPool.submit(conn.getTCPConnection().getReceiveTCP());
            threadPool.submit(conn.getUDPStream());

            conn.getTCPConnection().sendInit();

            while (true) { // delay added
                RemoteAPI.CommandData command;
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

                boolean isHolding = isHoldingBall();
                if (isHolding != prevHoldBallStatus) {
                    if (isHolding) {
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

    private RemoteAPI.CommandData createTDRDCmd() {
        return createPrimitiveCmdBuilder(TDRD);
    }

    /*** TL;DR ***/

    private RemoteAPI.CommandData createPrimitiveCmdBuilder(MotionMode mode) {
        RemoteAPI.CommandData.Builder command = RemoteAPI.CommandData.newBuilder();
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

    private RemoteAPI.CommandData createTDRVCmd() {
        return createPrimitiveCmdBuilder(TDRV);
    }

    private RemoteAPI.CommandData createTVRDCmd() {
        return createPrimitiveCmdBuilder(TVRD);
    }

    private RemoteAPI.CommandData createTVRVCmd() {
        return createPrimitiveCmdBuilder(TVRV);
    }

    private RemoteAPI.CommandData createNSTDRDCmd() {
        return createPrimitiveCmdBuilder(NSTDRD);
    }

    private RemoteAPI.CommandData createNSTDRVCmd() {
        return createPrimitiveCmdBuilder(NSTDRV);
    }

    private RemoteAPI.CommandData createAutoCapCmd() {
        RemoteAPI.CommandData.Builder command = RemoteAPI.CommandData.newBuilder();
        command.setIsWorldFrame(true);
        command.setEnableBallAutoCapture(true);
        command.setMode(TVRV.ordinal());

        RemoteAPI.Vec3D.Builder motionSetPoint = RemoteAPI.Vec3D.newBuilder();
        motionSetPoint.setX(0);
        motionSetPoint.setY(0);
        motionSetPoint.setZ(0);
        command.setMotionSetPoint(motionSetPoint);

        RemoteAPI.Vec2D.Builder kickerSetPoint = RemoteAPI.Vec2D.newBuilder();
        Vec2D kickVel = kickVelSub.getMsg();
        kickerSetPoint.setX(kickVel.x);
        kickerSetPoint.setY(kickVel.y);
        command.setKickerSetPoint(kickerSetPoint);

        return command.build();
    }

    public void displayPathFinder() {
        if (pathFinder instanceof JPSPathFinder) {
            ((JPSPathFinder) pathFinder).display();
        }
    }


}