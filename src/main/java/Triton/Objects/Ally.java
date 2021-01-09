package Triton.Objects;

import Proto.RemoteAPI;
import Triton.Algorithms.PathFinder.JPS.JPSPathFinder;
import Triton.Algorithms.PathFinder.PathFinder;
import Triton.Config.ObjectConfig;
import Triton.Config.PathfinderConfig;
import Triton.Dependencies.DesignPattern.PubSubSystem.*;
import Triton.Dependencies.Shape.Circle2D;
import Triton.Dependencies.Shape.Vec2D;
import Triton.Dependencies.Team;
import Triton.Modules.Detection.BallData;
import Triton.Modules.Detection.RobotData;
import Triton.Modules.RemoteStation.RobotConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

import static Triton.Objects.AllyState.*;
import static Triton.Objects.AllyState.TVRV_MOTION;


public class Ally extends Robot {
    private static final int TDRD = 0;
    private static final int TDRV = 1;
    private static final int TVRD = 2;
    private static final int TVRV = 3;
    private static final int NSTDRD = 4;
    private static final int NSTDRV = 5;

    private final RobotConnection conn;
    /*** external pub sub ***/
    private final Subscriber<HashMap<String, Integer>> fieldSizeSub;
    private final ArrayList<Subscriber<RobotData>> yellowRobotSubs;
    private final ArrayList<Subscriber<RobotData>> blueRobotSubs;
    private final Subscriber<BallData> ballSub;
    private final Subscriber<Boolean> dribStatSub;
    private final Publisher<RemoteAPI.Commands> commandsPub;
    /*** internal pub sub ***/

    private final Publisher<AllyState> statePub;
    private final Subscriber<AllyState> stateSub;
    private final Publisher<Vec2D> pointPub, kickVelPub;
    private final Subscriber<Vec2D> pointSub, kickVelSub;
    private final Publisher<Double> angPub;
    private final Subscriber<Double> angSub;

    protected ThreadPoolExecutor pool;
    private PathFinder pathFinder;
    private AllyState prevState;

    public Ally(Team team, int ID, ThreadPoolExecutor pool) {
        super(team, ID);
        this.pool = pool;

        conn = new RobotConnection(ID);

        fieldSizeSub = new FieldSubscriber<>("geometry", "fieldSize");

        blueRobotSubs = new ArrayList<>();
        yellowRobotSubs = new ArrayList<>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            blueRobotSubs.add(new FieldSubscriber<>("detection", Team.BLUE.name() + i));
            yellowRobotSubs.add(new FieldSubscriber<>("detection", Team.YELLOW.name() + i));
        }
        ballSub = new FieldSubscriber<>("detection", "ball");

        statePub = new FieldPublisher<>("Ally state", "" + ID, TVRV_MOTION);
        stateSub = new FieldSubscriber<>("Ally state", "" + ID);

        pointPub = new FieldPublisher<>("Ally point", "" + ID, new Vec2D(0, 0));
        pointSub = new FieldSubscriber<>("Ally point", "" + ID);
        angPub = new FieldPublisher<>("Ally ang", "" + ID, 0.0);
        angSub = new FieldSubscriber<>("Ally ang", "" + ID);
        kickVelPub = new FieldPublisher<>("Ally kickVel", "" + ID, new Vec2D(0, 0));
        kickVelSub = new FieldSubscriber<>("Ally kickVel", "" + ID);

        dribStatSub = new FieldSubscriber<>("Ally drib", "" + ID);
        commandsPub = new MQPublisher<>("commands", "" + ID);

        conn.buildTcpConnection();
        conn.buildCommandUDP();
        // conn.buildDataStream(port + ConnectionConfig.DATA_UDP_OFFSET);
        conn.buildVisionStream(team);
    }

    private void setState(AllyState state) {
        setState(state);
        prevState = state;
    }

    public boolean getDribblerStatus() {
        return dribStatSub.getMsg();
    }

    /*** primitive control methods ***/
    public void autoCap() {
        setState(AUTO_CAPTURE);
    }

    // Note: (moveTo/At & spinTo/At] are mutually exclusive to [pathTo & rotateTo]

    /**
     * @param loc player perspective, millimeter
     */
    public void moveTo(Vec2D loc) {
        switch (stateSub.getMsg()) {
            case TDRD_MOTION, TVRD_MOTION -> setState(TDRD_MOTION);
            default -> setState(TDRV_MOTION);
        }
        pointPub.publish(loc);
    }

    /**
     * @param vel player perspective, vector with unit as percentage from -100 to 100
     */
    public void moveAt(Vec2D vel) {
        switch (stateSub.getMsg()) {
            case TDRD_MOTION, TVRD_MOTION -> setState(TVRD_MOTION);
            default -> setState(TVRV_MOTION);
        }
        pointPub.publish(vel);
    }

    /**
     * @param angle player perspective, degrees, starting from y-axis, positive is counter clockwise
     */
    public void spinTo(double angle) {
        switch (stateSub.getMsg()) {
            case TDRD_MOTION, TDRV_MOTION -> setState(TDRD_MOTION);
            default -> setState(TVRD_MOTION);
        }
        angPub.publish(angle);
    }

    /**
     * @param angVel unit is percentage from -100 to 100, positive is counter clockwise
     */
    public void spinAt(double angVel) {
        switch (stateSub.getMsg()) {
            case TDRD_MOTION, TDRV_MOTION -> setState(TDRV_MOTION);
            default -> setState(TVRV_MOTION);
        }
        angPub.publish(angVel);
    }

    // runs in the caller thread
    public void kick(Vec2D kickVel) {
        kickVelPub.publish(kickVel);
    }

    /*** advanced control methods ***/
    /*** path control methods ***/
    public void pathTo(Vec2D endPoint, double angle) {
        setState(FOLLOW_PATH);
        pointPub.publish(endPoint);
        angPub.publish(angle);
    }

    public void sprintTo(Vec2D endPoint) {
        setState(SPRINT);
        pointPub.publish(endPoint);
    }

    public void rotateTo(double angle) {
        setState(ROTATE);
        angPub.publish(angle);
    }

    /*** ... methods ***/
    public void getBall() {
        if (prevState != GET_BALL) {
            conn.getTCPConnection().requestDribblerStatus();
        }

        setState(GET_BALL);
    }

    public void intercept() {
        setState(INTERCEPT);
    }

    public void pass() {
        setState(PASS);
    }

    // Everything in run() runs in the Ally Thread
    @Override
    public void run() {
        try {
            super.run();
            initPathfinder();

            conn.getTCPConnection().connect();
            conn.getTCPConnection().sendInit();

            pool.execute(conn.getTCPConnection());
            pool.execute(conn.getCommandStream());
            // pool.execute(conn.getDataStream());
            pool.execute(conn.getVisionStream());

            while (true) {
                publishCommand();
            }
        } catch (Exception e) {
            System.out.printf("Robot %d TCP connection fails: %s\n", super.ID, e.getClass());
            e.printStackTrace();
        }
    }

    @Override
    protected void subscribe() {
        super.subscribe();
        try {
            fieldSizeSub.subscribe();
            for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
                yellowRobotSubs.get(i).subscribe();
                blueRobotSubs.get(i).subscribe();
            }
            ballSub.subscribe();

            dribStatSub.subscribe();
            stateSub.subscribe();
            pointSub.subscribe();
            angSub.subscribe();
            kickVelSub.subscribe();
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

    /**
     * Publishes the next node in the set path of the robot
     */
    private void publishCommand() {
        RemoteAPI.Commands command;
        AllyState state = stateSub.getMsg();

        switch (state) {
            case TDRD_MOTION -> command = createTDRDCmd();
            case TDRV_MOTION -> command = createTDRVCmd();
            case TVRD_MOTION -> command = createTVRDCmd();
            case TVRV_MOTION -> command = createTVRVCmd();
            case AUTO_CAPTURE -> command = createAutoCapCmd();
            case FOLLOW_PATH -> command = createFollowPathCmd();
            case SPRINT -> command = createSprintCmd();
            case ROTATE -> command = createRotateCmd();
            case GET_BALL -> command = createGetBallCmd();
            case INTERCEPT -> command = createInterceptCmd();
            case PASS -> command = createPassCmd();
            default -> command = createTVRVCmd();
        }
        commandsPub.publish(command);
    }

    private RemoteAPI.Commands.Builder createPrimitiveCmdBuilder() {
        RemoteAPI.Commands.Builder command = RemoteAPI.Commands.newBuilder();
        command.setIsWorldFrame(true);
        command.setEnableBallAutoCapture(false);

        RemoteAPI.Vec3D.Builder motionSetPoint = RemoteAPI.Vec3D.newBuilder();
        Vec2D point = pointSub.getMsg();
        motionSetPoint.setX(point.x);
        motionSetPoint.setY(point.y);
        double angle = angSub.getMsg();
        angle = (angle > 180) ? angle - 360 : angle;
        angle = (angle < -180) ? angle + 360 : angle;
        motionSetPoint.setZ(angle);
        command.setMotionSetPoint(motionSetPoint);

        RemoteAPI.Vec2D.Builder kickerSetPoint = RemoteAPI.Vec2D.newBuilder();
        Vec2D kickVel = kickVelSub.getMsg();
        kickerSetPoint.setX(kickVel.x);
        kickerSetPoint.setY(kickVel.y);
        command.setKickerSetPoint(kickerSetPoint);

        return command;
    }

    private RemoteAPI.Commands createTDRDCmd() {
        RemoteAPI.Commands.Builder command = createPrimitiveCmdBuilder();
        return command.setMode(TDRD).build();
    }

    private RemoteAPI.Commands createTDRVCmd() {
        RemoteAPI.Commands.Builder command = createPrimitiveCmdBuilder();
        return command.setMode(TDRV).build();
    }

    private RemoteAPI.Commands createTVRDCmd() {
        RemoteAPI.Commands.Builder command = createPrimitiveCmdBuilder();
        return command.setMode(TVRD).build();
    }

    private RemoteAPI.Commands createTVRVCmd() {
        RemoteAPI.Commands.Builder command = createPrimitiveCmdBuilder();
        return command.setMode(TVRV).build();
    }

    private RemoteAPI.Commands createAutoCapCmd() {
        RemoteAPI.Commands.Builder command = RemoteAPI.Commands.newBuilder();
        command.setIsWorldFrame(true);
        command.setEnableBallAutoCapture(true);
        command.setMode(TVRV);

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

    private RemoteAPI.Commands createFollowPathCmd() {
        RemoteAPI.Commands.Builder command = RemoteAPI.Commands.newBuilder();
        command.setIsWorldFrame(true);
        command.setEnableBallAutoCapture(false);

        RemoteAPI.Vec3D.Builder motionSetPoint = RemoteAPI.Vec3D.newBuilder();

        double targetAngle = normAng(angSub.getMsg());
        double currAngle = getData().getAngle();
        double angDiff = calcAngDiff(targetAngle, currAngle);

        boolean usingRD = false;
        double absAngleDiff = Math.abs(angDiff);
        if (absAngleDiff <= PathfinderConfig.RD_SWITCH_ROTATE_ANGLE_THRESH) {
            usingRD = true;
            motionSetPoint.setZ(targetAngle);
        } else {
            motionSetPoint.setZ(Math.signum(angDiff) * 100);
        }

        if (absAngleDiff <= PathfinderConfig.MOVE_ANGLE_THRESH) {
            ArrayList<Vec2D> path = findPath(pointSub.getMsg());
            if (path != null && path.size() > 0) {
                Vec2D nextNode;
                if (path.size() == 1) {
                    if (usingRD)
                        command.setMode(TDRD);
                    else
                        command.setMode(TDRV);
                    nextNode = path.get(0);
                } else if (path.size() == 2) {
                    if (usingRD)
                        command.setMode(TDRD);
                    else
                        command.setMode(TDRV);
                    nextNode = path.get(1);
                } else {
                    if (usingRD)
                        command.setMode(NSTDRD);
                    else
                        command.setMode(NSTDRV);
                    nextNode = path.get(1);
                }
                motionSetPoint.setX(nextNode.x);
                motionSetPoint.setY(nextNode.y);
            } else {
                if (usingRD)
                    command.setMode(TVRD);
                else
                    command.setMode(TVRV);
                motionSetPoint.setX(0);
                motionSetPoint.setY(0);
            }
        } else {
            if (usingRD)
                command.setMode(TVRD);
            else
                command.setMode(TVRV);
            motionSetPoint.setX(0);
            motionSetPoint.setY(0);
        }
        command.setMotionSetPoint(motionSetPoint);

        RemoteAPI.Vec2D.Builder kickerSetPoint = RemoteAPI.Vec2D.newBuilder();
        Vec2D kickVel = kickVelSub.getMsg();
        kickerSetPoint.setX(kickVel.x);
        kickerSetPoint.setY(kickVel.y);
        command.setKickerSetPoint(kickerSetPoint);

        return command.build();
    }

    private RemoteAPI.Commands createSprintCmd() {
        RemoteAPI.Commands.Builder command = RemoteAPI.Commands.newBuilder();
        command.setIsWorldFrame(true);
        command.setEnableBallAutoCapture(false);

        RemoteAPI.Vec3D.Builder motionSetPoint = RemoteAPI.Vec3D.newBuilder();
        ArrayList<Vec2D> path = findPath(pointSub.getMsg());

        if (path != null && path.size() > 0) {
            double targetAngle = 0;
            Vec2D pos = getData().getPos();
            boolean rotatingToThresholdPoint = false;
            for (Vec2D node : path) {
                double dist = node.sub(pos).mag();
                if (dist >= PathfinderConfig.SPRINT_TO_ROTATE_DIST_THRESH) {
                    targetAngle = normAng(node.sub(pos).toPlayerAngle());
                    rotatingToThresholdPoint = true;
                    break;
                }
            }

            if (!rotatingToThresholdPoint) {
                Vec2D dest = path.get(path.size() - 1);
                targetAngle = dest.sub(pos).toPlayerAngle();
            }


            double currAngle = getData().getAngle();
            double angDiff = calcAngDiff(targetAngle, currAngle);

            boolean usingRD = false;
            double absAngleDiff = Math.abs(angDiff);
            if (absAngleDiff <= PathfinderConfig.RD_SWITCH_ROTATE_ANGLE_THRESH) {
                usingRD = true;
                motionSetPoint.setZ(targetAngle);
            } else {
                motionSetPoint.setZ(Math.signum(angDiff) * 100);
            }

            if (!rotatingToThresholdPoint || absAngleDiff <= PathfinderConfig.MOVE_ANGLE_THRESH) {
                Vec2D nextNode;
                if (path.size() == 1) {
                    if (usingRD)
                        command.setMode(TDRD);
                    else
                        command.setMode(TDRV);
                    nextNode = path.get(0);
                } else if (path.size() == 2) {
                    if (usingRD)
                        command.setMode(TDRD);
                    else
                        command.setMode(TDRV);
                    nextNode = path.get(1);
                } else {
                    if (usingRD)
                        command.setMode(NSTDRD);
                    else
                        command.setMode(NSTDRV);
                    nextNode = path.get(1);
                }
                motionSetPoint.setX(nextNode.x);
                motionSetPoint.setY(nextNode.y);
            } else {
                if (usingRD)
                    command.setMode(TVRD);
                else
                    command.setMode(TVRV);
                motionSetPoint.setX(0);
                motionSetPoint.setY(0);
            }
        } else {
            command.setMode(TVRV);
            motionSetPoint.setX(0);
            motionSetPoint.setY(0);
            motionSetPoint.setZ(0);
        }
        command.setMotionSetPoint(motionSetPoint);

        RemoteAPI.Vec2D.Builder kickerSetPoint = RemoteAPI.Vec2D.newBuilder();
        Vec2D kickVel = kickVelSub.getMsg();
        kickerSetPoint.setX(kickVel.x);
        kickerSetPoint.setY(kickVel.y);
        command.setKickerSetPoint(kickerSetPoint);

        return command.build();
    }

    private RemoteAPI.Commands createRotateCmd() {
        RemoteAPI.Commands.Builder command = RemoteAPI.Commands.newBuilder();
        command.setIsWorldFrame(true);
        command.setEnableBallAutoCapture(false);

        RemoteAPI.Vec3D.Builder motionSetPoint = RemoteAPI.Vec3D.newBuilder();
        motionSetPoint.setX(0);
        motionSetPoint.setY(0);

        double targetAngle = normAng(angSub.getMsg());
        double currAngle = getData().getAngle();
        double angDiff = calcAngDiff(targetAngle, currAngle);

        double absAngleDiff = Math.abs(angDiff);
        if (absAngleDiff <= PathfinderConfig.RD_SWITCH_ROTATE_ANGLE_THRESH) {
            command.setMode(TVRD);
            motionSetPoint.setZ(targetAngle);
        } else {
            command.setMode(TVRV);
            motionSetPoint.setZ(Math.signum(angDiff) * 100);
        }
        command.setMotionSetPoint(motionSetPoint);

        RemoteAPI.Vec2D.Builder kickerSetPoint = RemoteAPI.Vec2D.newBuilder();
        Vec2D kickVel = kickVelSub.getMsg();
        kickerSetPoint.setX(kickVel.x);
        kickerSetPoint.setY(kickVel.y);
        command.setKickerSetPoint(kickerSetPoint);

        return command.build();
    }

    private RemoteAPI.Commands createGetBallCmd() {
        Vec2D ballPos = ballSub.getMsg().getPos();
        Vec2D currPos = getData().getPos();
        Vec2D currPosToBall = ballPos.sub(currPos);
        if (currPosToBall.mag() <= PathfinderConfig.BALL_CAP_DIST_THRESH) {
            return createAutoCapCmd();
        } else {
            pointPub.publish(ballPos);
            angPub.publish(currPosToBall.toPlayerAngle());
            return createFollowPathCmd();
        }
    }

    private RemoteAPI.Commands createInterceptCmd() {
        return null;
    }

    private RemoteAPI.Commands createPassCmd() {
        return null;
    }

    private double normAng(double ang) {
        ang = (ang > 180) ? ang - 360 : ang;
        ang = (ang < -180) ? ang + 360 : ang;
        return ang;
    }

    private double calcAngDiff(double angA, double angB) {
        return normAng(angB - angA);
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
        return pathFinder.findPath(getData().getPos(), endPoint);
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
}