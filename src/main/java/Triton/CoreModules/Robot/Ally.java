package Triton.CoreModules.Robot;

import Proto.RemoteAPI;
import Triton.CoreModules.AI.Algorithms.PathFinder.JumpPointSearch.JPSPathFinder;
import Triton.CoreModules.AI.Algorithms.PathFinder.PathFinder;
import Triton.Config.ObjectConfig;
import Triton.Config.PathfinderConfig;
import Triton.Misc.DesignPattern.PubSubSystem.*;
import Triton.Misc.Geometry.Circle2D;
import Triton.Misc.Coordinates.Vec2D;
import Triton.PeriphModules.Detection.BallData;
import Triton.PeriphModules.Detection.RobotData;
import Triton.CoreModules.Robot.RobotSockets.RobotConnection;

import static Triton.CoreModules.Robot.AllyState.*;
import static Triton.CoreModules.Robot.AllyState.MOVE_TDRD;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

import static Triton.Misc.Coordinates.PerspectiveConverter.calcAngDiff;
import static Triton.Misc.Coordinates.PerspectiveConverter.normAng;


public class Ally extends Robot {
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

    protected ThreadPoolExecutor threadPool;
    private PathFinder pathFinder;

    public Ally(Team team, int ID, ThreadPoolExecutor threadPool) {
        super(team, ID);
        this.threadPool = threadPool;

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
    
    public boolean connect() {
        // To-do
        return false;
    }

    public boolean setOrigin() {
        // To-do
        return false;
    }


    // Everything in run() runs in the Ally Thread
    @Override
    public void run() {
        try {
            super.run();
            initPathfinder();

            conn.getTCPConnection().connect();
            conn.getTCPConnection().sendInit();

            threadPool.submit(conn.getTCPConnection());
            threadPool.submit(conn.getTCPConnection().getSendTCP());
            threadPool.submit(conn.getTCPConnection().getReceiveTCP());
            threadPool.submit(conn.getCommandStream());
            // threadPool.execute(conn.getDataStream());
            threadPool.submit(conn.getVisionStream());

            while (true) {
                publishCommand();
            }
        } catch (Exception e) {
            if(e instanceof IOException) {
                System.out.printf("Robot %d TCP connection fails: %s\n", super.ID, e.getClass());
            }
            else {
                e.printStackTrace();
            }
        }
    }



    public boolean getDribblerStatus() {
        subscribe();
        return dribStatSub.getMsg();
    }

    /*** primitive control methods ***/
    public void autoCap() {
        statePub.publish(AUTO_CAPTURE);
    }

    // Note: (moveTo/At & spinTo/At] are mutually exclusive to [pathTo & rotateTo]

    /**
     * @param loc player perspective, millimeter
     */
    public void moveTo(Vec2D loc) {
        switch (stateSub.getMsg()) {
            case MOVE_TDRD, MOVE_TVRD -> statePub.publish(MOVE_TDRD);
            default -> statePub.publish(MOVE_TDRV);
        }
        pointPub.publish(loc);
    }

    /**
     * @param vel player perspective, vector with unit as percentage from -100 to 100
     */
    public void moveAt(Vec2D vel) {
        switch (stateSub.getMsg()) {
            case MOVE_TDRD, MOVE_TVRD -> statePub.publish(MOVE_TVRD);
            default -> statePub.publish(MOVE_TVRV);
        }
        pointPub.publish(vel);
    }

    /**
     * @param angle player perspective, degrees, starting from y-axis, positive is counter clockwise
     */
    public void spinTo(double angle) {
        switch (stateSub.getMsg()) {
            case MOVE_TDRD, MOVE_TDRV -> statePub.publish(MOVE_TDRD);
            default -> statePub.publish(MOVE_TVRD);
        }
        angPub.publish(angle);
    }

    /**
     * @param angVel unit is percentage from -100 to 100, positive is counter clockwise
     */
    public void spinAt(double angVel) {
        switch (stateSub.getMsg()) {
            case MOVE_TDRD, MOVE_TDRV -> statePub.publish(MOVE_TDRV);
            default -> statePub.publish(MOVE_TVRV);
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
        statePub.publish(FOLLOW_PATH);
        pointPub.publish(endPoint);
        angPub.publish(angle);
    }

    public void sprintTo(Vec2D endPoint) {
        statePub.publish(SPRINT);
        pointPub.publish(endPoint);
    }

    public void sprintToAngle(Vec2D endPoint, double angle) {
        statePub.publish(SPRINT_ANGLE);
        pointPub.publish(endPoint);
        angPub.publish(angle);
    }

    public void rotateTo(double angle) {
        statePub.publish(ROTATE);
        angPub.publish(angle);
    }

    /*** Skills methods ***/
    public void getBall() {
        statePub.publish(GET_BALL);
    }

    public void receiveBall(Vec2D receivePoint) {
        statePub.publish(RECEIVE_BALL);
        pointPub.publish(receivePoint);
    }

    public void intercept() {
        statePub.publish(INTERCEPT);
    }

    public void pass() {
        statePub.publish(PASS);
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
            case MOVE_TDRD -> command = createTDRDCmd();
            case MOVE_TDRV -> command = createTDRVCmd();
            case MOVE_TVRD -> command = createTVRDCmd();
            case MOVE_TVRV -> command = createTVRVCmd();
            case AUTO_CAPTURE -> command = createAutoCapCmd();
            case FOLLOW_PATH -> command = createFollowPathCmd();
            case SPRINT -> command = createSprintCmd();
            case SPRINT_ANGLE -> command = createSprintAngleCmd();
            case ROTATE -> command = createRotateCmd();
            case GET_BALL -> command = createGetBallCmd();
            case RECEIVE_BALL -> command = receiveBallCmd();
            case INTERCEPT -> command = createInterceptCmd();
            case PASS -> command = createPassCmd();
            default -> command = createTVRVCmd();
        }
        commandsPub.publish(command);
    }

    private RemoteAPI.Commands createPrimitiveCmdBuilder(MoveMode mode) {
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
        return createPrimitiveCmdBuilder(MoveMode.TDRD);
    }

    private RemoteAPI.Commands createTDRVCmd() {
        return createPrimitiveCmdBuilder(MoveMode.TDRV);
    }

    private RemoteAPI.Commands createTVRDCmd() {
        return createPrimitiveCmdBuilder(MoveMode.TVRD);
    }

    private RemoteAPI.Commands createTVRVCmd() {
        return createPrimitiveCmdBuilder(MoveMode.TVRV);
    }

    private RemoteAPI.Commands createAutoCapCmd() {
        RemoteAPI.Commands.Builder command = RemoteAPI.Commands.newBuilder();
        command.setIsWorldFrame(true);
        command.setEnableBallAutoCapture(true);
        command.setMode(MoveMode.TVRV.ordinal());

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
        if (absAngleDiff <= PathfinderConfig.RD_ANGLE_THRESH) {
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
                    nextNode = path.get(0);
                } else if (path.size() == 2) {
                    nextNode = path.get(1);
                } else {
                    nextNode = path.get(1);
                }

                motionSetPoint.setX(nextNode.x);
                motionSetPoint.setY(nextNode.y);

                if (path.size() <= 2) {
                    if (usingRD)
                        command.setMode(MoveMode.TDRD.ordinal());
                    else
                        command.setMode(MoveMode.TDRV.ordinal());
                } else {
                    if (usingRD)
                        command.setMode(MoveMode.NSTDRD.ordinal());
                    else
                        command.setMode(MoveMode.NSTDRV.ordinal());
                }
            } else {
                motionSetPoint.setX(0);
                motionSetPoint.setY(0);

                if (usingRD)
                    command.setMode(MoveMode.TVRD.ordinal());
                else
                    command.setMode(MoveMode.TVRV.ordinal());
            }
        } else {
            motionSetPoint.setX(0);
            motionSetPoint.setY(0);

            if (usingRD)
                command.setMode(MoveMode.TVRD.ordinal());
            else
                command.setMode(MoveMode.TVRV.ordinal());
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
            Vec2D allyPos = getData().getPos();
            boolean targetAngleFound = false;
            for (Vec2D node : path) {
                double dist = node.sub(allyPos).mag();
                if (dist >= PathfinderConfig.SPRINT_TO_ROTATE_DIST_THRESH) {
                    targetAngle = node.sub(allyPos).toPlayerAngle();
                    targetAngleFound = true;
                    break;
                }
            }

            Vec2D nextNode;
            if (path.size() == 1)
                nextNode = path.get(0);
            else if (path.size() == 2)
                nextNode = path.get(1);
            else
                nextNode = path.get(1);

            if (targetAngleFound) {
                double currAngle = getData().getAngle();
                double angDiff = calcAngDiff(targetAngle, currAngle);

                double absAngleDiff = Math.abs(angDiff);
                boolean usingRD = false;

                if (absAngleDiff <= PathfinderConfig.RD_ANGLE_THRESH) {
                    usingRD = true;
                    motionSetPoint.setZ(targetAngle);
                } else {
                    motionSetPoint.setZ(Math.signum(angDiff) * 100);
                }

                if (absAngleDiff <= PathfinderConfig.MOVE_ANGLE_THRESH) {
                    motionSetPoint.setX(nextNode.x);
                    motionSetPoint.setY(nextNode.y);

                    if (path.size() <= 2) {
                        if (usingRD)
                            command.setMode(MoveMode.TDRD.ordinal());
                        else
                            command.setMode(MoveMode.TDRV.ordinal());
                    } else {
                        if (usingRD)
                            command.setMode(MoveMode.NSTDRD.ordinal());
                        else
                            command.setMode(MoveMode.NSTDRV.ordinal());
                    }
                } else {
                    motionSetPoint.setX(0);
                    motionSetPoint.setY(0);

                    if (usingRD)
                        command.setMode(MoveMode.TVRD.ordinal());
                    else
                        command.setMode(MoveMode.TVRV.ordinal());
                }
            } else {
                command.setMode(MoveMode.TDRV.ordinal());
                motionSetPoint.setX(nextNode.x);
                motionSetPoint.setY(nextNode.y);
                motionSetPoint.setZ(0);
            }
        } else {
            command.setMode(MoveMode.TVRV.ordinal());
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

    private RemoteAPI.Commands createSprintAngleCmd() {
        RemoteAPI.Commands.Builder command = RemoteAPI.Commands.newBuilder();
        command.setIsWorldFrame(true);
        command.setEnableBallAutoCapture(false);

        RemoteAPI.Vec3D.Builder motionSetPoint = RemoteAPI.Vec3D.newBuilder();
        ArrayList<Vec2D> path = findPath(pointSub.getMsg());
        if (path != null && path.size() > 0) {
            double targetAngle = 0;
            Vec2D allyPos = getData().getPos();
            boolean targetAngleFound = false;
            for (Vec2D node : path) {
                double dist = node.sub(allyPos).mag();
                if (dist >= PathfinderConfig.SPRINT_TO_ROTATE_DIST_THRESH) {
                    targetAngle = node.sub(allyPos).toPlayerAngle();
                    targetAngleFound = true;
                    break;
                }
            }

            if (!targetAngleFound) {
                targetAngle = angSub.getMsg();
            }

            Vec2D nextNode;
            if (path.size() == 1)
                nextNode = path.get(0);
            else if (path.size() == 2)
                nextNode = path.get(1);
            else
                nextNode = path.get(1);

            double currAngle = getData().getAngle();
            double angDiff = calcAngDiff(targetAngle, currAngle);

            double absAngleDiff = Math.abs(angDiff);
            boolean usingRD = false;

            if (absAngleDiff <= PathfinderConfig.RD_ANGLE_THRESH) {
                usingRD = true;
                motionSetPoint.setZ(targetAngle);
            } else {
                motionSetPoint.setZ(Math.signum(angDiff) * 100);
            }

            if (absAngleDiff <= PathfinderConfig.MOVE_ANGLE_THRESH) {
                motionSetPoint.setX(nextNode.x);
                motionSetPoint.setY(nextNode.y);

                if (path.size() <= 2) {
                    if (usingRD)
                        command.setMode(MoveMode.TDRD.ordinal());
                    else
                        command.setMode(MoveMode.TDRV.ordinal());
                } else {
                    if (usingRD)
                        command.setMode(MoveMode.NSTDRD.ordinal());
                    else
                        command.setMode(MoveMode.NSTDRV.ordinal());
                }
            } else {
                motionSetPoint.setX(0);
                motionSetPoint.setY(0);

                if (usingRD)
                    command.setMode(MoveMode.TVRD.ordinal());
                else
                    command.setMode(MoveMode.TVRV.ordinal());
            }
        } else {
            command.setMode(MoveMode.TVRV.ordinal());
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
        if (absAngleDiff <= PathfinderConfig.RD_ANGLE_THRESH) {
            command.setMode(MoveMode.TVRD.ordinal());
            motionSetPoint.setZ(targetAngle);
        } else {
            command.setMode(MoveMode.TVRV.ordinal());
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
        if (currPosToBall.mag() <= PathfinderConfig.AUTOCAP_DIST_THRESH) {
            return createAutoCapCmd();
        } else {
            pointPub.publish(ballPos);
            angPub.publish(currPosToBall.toPlayerAngle());
            return createSprintAngleCmd();
        }
    }

    private RemoteAPI.Commands receiveBallCmd() {
        Vec2D ballPos = ballSub.getMsg().getPos();
        Vec2D currPos = getData().getPos();
        double dist = ballPos.sub(currPos).mag();
        if (dist <= PathfinderConfig.AUTOCAP_DIST_THRESH) {
            return createAutoCapCmd();
        } else {
            double targetAngle = ballPos.sub(currPos).toPlayerAngle();
            angPub.publish(targetAngle);
            return createSprintAngleCmd();
        }
    }

    private RemoteAPI.Commands createInterceptCmd() {
        return null;
    }

    private RemoteAPI.Commands createPassCmd() {
        return null;
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