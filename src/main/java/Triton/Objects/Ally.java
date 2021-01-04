package Triton.Objects;

import Proto.RemoteAPI;
import Triton.Algorithms.PathFinder.JPS.JPSPathFinder;
import Triton.Algorithms.PathFinder.PathFinder;
import Triton.Config.ObjectConfig;
import Triton.Dependencies.DesignPattern.PubSubSystem.*;
import Triton.Dependencies.Shape.Circle2D;
import Triton.Dependencies.Shape.Vec2D;
import Triton.Modules.Detection.RobotData;
import Triton.Dependencies.Team;
import Triton.Modules.RemoteStation.RobotConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;


public class Ally extends Robot {
    private final RobotConnection conn;
    protected ThreadPoolExecutor pool;
    private PathFinder pathFinder;
    private final Subscriber<HashMap<String, Integer>> fieldSizeSub;
    private final ArrayList<Subscriber<RobotData>> yellowRobotSubs;
    private final ArrayList<Subscriber<RobotData>> blueRobotSubs;
    private final Publisher<RemoteAPI.Commands> commandsPub;

    private boolean usePrimitiveCommands;
    private boolean useAsVel = true;
    private boolean useAsAngVel = true;

    private boolean autoCap;
    private Vec2D point;
    private ArrayList<Vec2D> path;
    private Double angle;
    private Vec2D kickVel;

    private final Publisher<Boolean> autoCapPub;
    private final Subscriber<Boolean> autoCapSub;
    private final Publisher<Vec2D> pointPub;
    private final Subscriber<Vec2D> pointSub;
    private final Publisher<Double> angPub;
    private final Subscriber<Double> angSub;
    private final Publisher<Vec2D> kickVelPub;
    private final Subscriber<Vec2D> kickVelSub;

    public Ally(Team team, int ID, ThreadPoolExecutor pool) {
        super(team, ID);
        this.pool = pool;

        conn = new RobotConnection(ID);

        String name = (team == Team.YELLOW) ? "yellow robot data" + ID : "blue robot data" + ID;
        fieldSizeSub = new FieldSubscriber<>("geometry", "fieldSize");

        yellowRobotSubs = new ArrayList<>();
        blueRobotSubs = new ArrayList<>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobotSubs.add(new FieldSubscriber<>("detection", "yellow robot data" + i));
            blueRobotSubs.add(new FieldSubscriber<>("detection", "blue robot data" + i));
        }

        autoCapPub = new FieldPublisher<>("Ally autoCap", "" + ID, null);
        autoCapSub = new FieldSubscriber<>("Ally autoCap", "" + ID);

        pointPub = new FieldPublisher<>("Ally point", "" + ID, null);
        pointSub = new FieldSubscriber<>("Ally point", "" + ID);

        angPub = new FieldPublisher<>("Ally ang", "" + ID, null);
        angSub = new FieldSubscriber<>("Ally ang", "" + ID);

        kickVelPub = new FieldPublisher<>("Ally kickVel", "" + ID, null);
        kickVelSub = new FieldSubscriber<>("Ally kickVel", "" + ID);

        commandsPub = new MQPublisher<>("commands", "" + ID);

        conn.buildTcpConnection();
        conn.buildCommandUDP();
        // conn.buildDataStream(port + ConnectionConfig.DATA_UDP_OFFSET);
        conn.buildVisionStream(team);
    }

    /*** primitive control methods ***/
    public void setAutoCap(boolean enable) {
        autoCapPub.publish(enable);
    }

    // Note: (moveTo/At & spinTo/At] are mutually exclusive to [pathTo & rotateTo]

    /**
     * @param loc player perspective, millimeter
     */
    public void moveTo(Vec2D loc) {
        usePrimitiveCommands = true;
        useAsVel = false;
        pointPub.publish(loc);
    }

    /**
     * @param vel player perspective, vector with unit as percentage from -100 to 100
     */
    public void moveAt(Vec2D vel) {
        usePrimitiveCommands = true;
        useAsVel = true;
        pointPub.publish(vel);
    }

    /**
     * @param angle player perspective, degrees, starting from y-axis, positive is counter clockwise
     */
    public void spinTo(double angle) {
        usePrimitiveCommands = true;
        useAsAngVel = false;
        angPub.publish(angle);
    }

    /**
     * @param angVel unit is percentage from -100 to 100, positive is counter clockwise
     */
    public void spinAt(double angVel) {
        usePrimitiveCommands = true;
        useAsAngVel = true;
        angPub.publish(angVel);
    }

    // runs in the caller thread
    public void kick(Vec2D kickVel) {
        kickVelPub.publish(kickVel);
    }

    /*** advanced control methods ***/
    // runs in the caller thread
    public void pathTo(Vec2D endPoint, double angle) {
        usePrimitiveCommands = false;
        pointPub.publish(endPoint);
        angPub.publish(angle);
    }

    public void rotateTo(double angle) {
        usePrimitiveCommands = false;
    }

    public void getBall() {
        usePrimitiveCommands = false;
    }

    public void intercept() {
        usePrimitiveCommands = false;
    }

    public void pass() {
        usePrimitiveCommands = false;
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
            autoCapSub.subscribe();
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
        if (usePrimitiveCommands) {
            command = createPrimitiveCommand();
        } else {
            command = createAdvancedCommand();
        }
        commandsPub.publish(command);
    }

    private RemoteAPI.Commands createPrimitiveCommand() {
        RemoteAPI.Commands.Builder command = RemoteAPI.Commands.newBuilder();
        command.setIsWorldFrame(true);

        Boolean autoCap = autoCapSub.getMsg();
        if (autoCap != null) {
            command.setEnableBallAutoCapture(autoCap);
        } else {
            command.setEnableBallAutoCapture(false);
        }

        int mode;
        if (useAsVel) {
            if (useAsAngVel) {
                mode = 3; // TVRV
            }
            else {
                mode = 2; // TVRD
            }
        } else {
            if (useAsAngVel) {
                mode = 1; // TDRV
            }
            else {
                mode = 0; // TDRD
            }
        }
        command.setMode(mode);

        RemoteAPI.Vec3D.Builder motionSetPoint = RemoteAPI.Vec3D.newBuilder();
        Vec2D point = pointSub.getMsg();
        if (point != null) {
            motionSetPoint.setX(point.x);
            motionSetPoint.setY(point.y);
        }

        Double ang = angSub.getMsg();
        motionSetPoint.setZ(ang != null ? ang : 0);

        command.setMotionSetPoint(motionSetPoint);

        RemoteAPI.Vec2D.Builder kickerSetPoint = RemoteAPI.Vec2D.newBuilder();
        Vec2D kickVel = kickVelSub.getMsg();
        if (kickVel != null) {
            kickerSetPoint.setX(kickVel.x);
            kickerSetPoint.setY(kickVel.y);
        } else {
            kickerSetPoint.setX(0);
            kickerSetPoint.setY(0);
        }
        command.setKickerSetPoint(kickerSetPoint);

        return command.build();
    }

    private RemoteAPI.Commands createAdvancedCommand() {
        RemoteAPI.Commands.Builder command = RemoteAPI.Commands.newBuilder();
        command.setIsWorldFrame(true);

        Boolean autoCap = autoCapSub.getMsg();
        command.setEnableBallAutoCapture(Objects.requireNonNullElse(autoCap, false));

        RemoteAPI.Vec3D.Builder motionSetPoint = RemoteAPI.Vec3D.newBuilder();
        ArrayList<Vec2D> path =  findPath();
        if (path != null && path.size() > 0) {
            Vec2D nextNode;
            if (path.size() == 1) {
                command.setMode(0);
                nextNode = path.get(0);
            } else {
                command.setMode(4);
                nextNode = path.get(1);
            }
            motionSetPoint.setX(nextNode.x);
            motionSetPoint.setY(nextNode.y);
        } else {
            command.setMode(3);
            motionSetPoint.setX(0);
            motionSetPoint.setY(0);
        }

        Double angle = angSub.getMsg();
        motionSetPoint.setZ(angle != null ? angle : 0);

        command.setMotionSetPoint(motionSetPoint);

        RemoteAPI.Vec2D.Builder kickerSetPoint = RemoteAPI.Vec2D.newBuilder();
        Vec2D kickVel = kickVelSub.getMsg();
        if (kickVel != null) {
            kickerSetPoint.setX(kickVel.x);
            kickerSetPoint.setY(kickVel.y);
        } else {
            kickerSetPoint.setX(0);
            kickerSetPoint.setY(0);
        }
        command.setKickerSetPoint(kickerSetPoint);
        return command.build();
    }

    public void displayPathFinder() {
        if (pathFinder instanceof JPSPathFinder) {
            ((JPSPathFinder) pathFinder).display();
        }
    }

    /**
     * Update the set path of the robot
     */
    private ArrayList<Vec2D> findPath() {
        Vec2D endPoint = pointSub.getMsg();
        if (endPoint == null)
            return null;

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