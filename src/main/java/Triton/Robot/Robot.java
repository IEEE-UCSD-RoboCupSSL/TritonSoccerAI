package Triton.Robot;

import Proto.MessagesRobocupSslGeometry.SSL_GeometryFieldSize;
import Proto.RemoteAPI.Commands;
import Proto.RemoteAPI.Vec3D;
import Triton.Computation.PathFinder.JPS.JPSPathFinder;
import Triton.Computation.PathFinder.PathFinder;
import Triton.Config.ConnectionConfig;
import Triton.Config.ObjectConfig;
import Triton.DesignPattern.PubSubSystem.Module;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.Detection.RobotData;
import Triton.Detection.Team;
import Triton.RemoteStation.RobotConnection;
import Triton.Shape.Circle2D;
import Triton.Shape.Vec2D;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Representation of a single robot
 */
public class Robot implements Module {
    private final Team team;
    private final int ID;
    private final ThreadPoolExecutor pool;
    private final RobotConnection conn;
    private final Subscriber<RobotData> robotDataSub;
    private final Subscriber<SSL_GeometryFieldSize> fieldSizeSub;
    private final ArrayList<Subscriber<RobotData>> yellowRobotSubs;
    private final ArrayList<Subscriber<RobotData>> blueRobotSubs;
    private final Subscriber<Pair<Vec2D, Double>> endPointSub;
    private RobotData data;
    private PathFinder pathFinder;
    private Publisher<Commands> commandsPub;

    private ArrayList<Vec2D> path;
    private double angle;

    /**
     * Construct a robot with specified team and ID
     * @param team team robot belongs to
     * @param ID ID of the robot
     * @param pool thread pool to run submodules on
     */
    public Robot(Team team, int ID, ThreadPoolExecutor pool) {
        this.team = team;
        this.ID = ID;
        this.pool = pool;

        String ip = "";
        int port = 0;
        switch (ID) {
            case 0 -> {
                ip = ConnectionConfig.ROBOT_0_IP.getValue0();
                port = ConnectionConfig.ROBOT_0_IP.getValue1();
            }
            case 1 -> {
                ip = ConnectionConfig.ROBOT_1_IP.getValue0();
                port = ConnectionConfig.ROBOT_1_IP.getValue1();
            }
            case 2 -> {
                ip = ConnectionConfig.ROBOT_2_IP.getValue0();
                port = ConnectionConfig.ROBOT_2_IP.getValue1();
            }
            case 3 -> {
                ip = ConnectionConfig.ROBOT_3_IP.getValue0();
                port = ConnectionConfig.ROBOT_3_IP.getValue1();
            }
            case 4 -> {
                ip = ConnectionConfig.ROBOT_4_IP.getValue0();
                port = ConnectionConfig.ROBOT_4_IP.getValue1();
            }
            case 5 -> {
                ip = ConnectionConfig.ROBOT_5_IP.getValue0();
                port = ConnectionConfig.ROBOT_5_IP.getValue1();
            }
            default -> System.out.println("Invalid Robot ID");
        }

        conn = new RobotConnection(ID);

        String name = (team == Team.YELLOW) ? "yellow robot data" + ID : "blue robot data" + ID;
        robotDataSub = new FieldSubscriber<>("detection", name);
        fieldSizeSub = new FieldSubscriber<>("geometry", "fieldSize");

        yellowRobotSubs = new ArrayList<>();
        blueRobotSubs = new ArrayList<>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobotSubs.add(new FieldSubscriber<>("detection", "yellow robot data" + i));
            blueRobotSubs.add(new FieldSubscriber<>("detection", "blue robot data" + i));
        }

        endPointSub = new FieldSubscriber<>("endPoint", "" + ID);

        if (team == ObjectConfig.MY_TEAM && ID == 0) {
            conn.buildTcpConnection(ip, port + ConnectionConfig.TCP_OFFSET);
            conn.buildCommandUDP(ip, port + ConnectionConfig.COMMAND_UDP_OFFSET);
            // conn.buildVisionStream(ip, port + ConnectionConfig.VISION_UDP_OFFSET);
            // conn.buildDataStream(port + ConnectionConfig.DATA_UDP_OFFSET);
            commandsPub = new MQPublisher<>("commands", "" + ID);
        }
    }

    /**
     * Run connections on thread pool, update paths and publish if robot is on our own team
     */
    @Override
    public void run() {
        try {
            subscribe();
            initPathfinder();

            if (team == ObjectConfig.MY_TEAM && ID == 0) {
                conn.getTCPConnection().connect();
                conn.getTCPConnection().sendInit();

                pool.execute(conn.getTCPConnection());
                pool.execute(conn.getCommandStream());
                // pool.execute(conn.getVisionStream());
                // pool.execute(conn.getDataStream());
            }

            while (true) {
                data = robotDataSub.getMsg();
                if (team == ObjectConfig.MY_TEAM && ID == 0) {
                    updatePath();
                    publishNextNode();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Subscribe to publishers
     */
    private void subscribe() {
        try {
            robotDataSub.subscribe(1000);
            fieldSizeSub.subscribe(1000);
            for (Subscriber<RobotData> robotSub : yellowRobotSubs)
                robotSub.subscribe(1000);
            for (Subscriber<RobotData> robotSub : blueRobotSubs)
                robotSub.subscribe(1000);

            if (team == ObjectConfig.MY_TEAM)
                endPointSub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize pathfinder with geometry information
     */
    private void initPathfinder() {
        while (pathFinder == null) {
            SSL_GeometryFieldSize fieldSize = fieldSizeSub.getMsg();

            if (fieldSize == null || fieldSize.getFieldLength() == 0 || fieldSize.getFieldWidth() == 0
                    || fieldSize.getGoalDepth() == 0)
                continue;

            double worldSizeX = fieldSize.getFieldLength();
            double worldSizeY = fieldSize.getFieldWidth();

            pathFinder = new JPSPathFinder(worldSizeX, worldSizeY);
        }
    }

    /**
     * Update the set path of the robot
     */
    public void updatePath() {
        Pair<Vec2D, Double> endPointPair = endPointSub.getMsg();
        if (endPointPair == null)
            return;

        Vec2D endPoint = endPointPair.getValue0();
        angle = endPointPair.getValue1();

        pathFinder.setObstacles(getObstacles());
        ArrayList<Vec2D> newPath = pathFinder.findPath(data.getPos(), endPoint);
        if (newPath != null)
            path = newPath;
    }

    /**
     * Returns an ArrayList of circles representing the obstacles for pathfinding
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

    /**
     * Publishes the next node in the set path of the robot
     */
    private void publishNextNode() {
        if (path == null || path.size() <= 1)
            return;

        Vec2D nextNode = path.get(1);
        Commands.Builder command = Commands.newBuilder();
        command.setMode(0);
        command.setIsWorldFrame(true);
        Vec3D.Builder dest = Vec3D.newBuilder();
        dest.setX(-nextNode.y);
        dest.setY(nextNode.x);
        dest.setZ(angle);
        command.setMotionSetPoint(dest);
        commandsPub.publish(command.build());
    }

    /**
     * @return the team the robot belongs to
     */
    public Team getTeam() {
        return this.team;
    }

    /**
     * @return the ID of the robot
     */
    public int getID() {
        return this.ID;
    }

    /**
     * @return the RobotConnection object this robot is using
     */
    public RobotConnection getRobotConnection() {
        return conn;
    }
}