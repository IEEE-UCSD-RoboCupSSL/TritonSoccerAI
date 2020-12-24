package Triton.Detection;

import Proto.MessagesRobocupSslGeometry.SSL_GeometryFieldSize;
import Proto.RemoteAPI.Commands;
import Proto.RemoteAPI.Vec3D;
import Triton.Computation.PathFinder.JPS.JPSPathFinder;
import Triton.Computation.PathFinder.PathFinder;
import Triton.Config.ConnectionConfig;
import Triton.Config.ObjectConfig;
import Triton.DesignPattern.PubSubSystem.Module;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.RemoteStation.RobotConnection;
import Triton.Shape.Circle2D;
import Triton.Shape.Vec2D;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

public class Robot implements Module {
    private final Team team;
    private final int ID;
    private final ThreadPoolExecutor pool;

    private RobotData data;
    private final RobotConnection conn;

    private PathFinder pathFinder;

    private final Subscriber<RobotData> robotDataSub;
    private final Subscriber<SSL_GeometryFieldSize> fieldSizeSub;
    private final ArrayList<Subscriber<RobotData>> yellowRobotSubs;
    private final ArrayList<Subscriber<RobotData>> blueRobotSubs;
    private final Subscriber<Pair<Vec2D, Double>> endPointSub;
    private Publisher<Commands> commandsPub;

    private ArrayList<Vec2D> path;
    private double angle;

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
            initPathfinder();
        }
    }

    private void subscribe() {
        try {
            robotDataSub.subscribe(1000);
            fieldSizeSub.subscribe(1000);
            for (Subscriber<RobotData> robotSub : yellowRobotSubs)
                robotSub.subscribe(1000);
            for (Subscriber<RobotData> robotSub : blueRobotSubs)
                robotSub.subscribe(1000);
            endPointSub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPathfinder() {
        try {
            fieldSizeSub.subscribe(1000);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
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

    public Team getTeam() {
        return this.team;
    }

    public int getID() {
        return this.ID;
    }

    public RobotData getRobotData() {
        return data;
    }

    public RobotConnection getRobotConnection() {
        return conn;
    }

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
    public void run() {
        try {
            subscribe();

            if (team == ObjectConfig.MY_TEAM && ID == 0) {
                conn.getTCPConnection().connect();
                conn.getTCPConnection().sendInit();

                pool.execute(conn.getTCPConnection());
                pool.execute(conn.getCommandStream());
                // pool.execute(conn.getVisionStream());
                // pool.execute(conn.getDataStream());
            }

            while (true) {
                setData(robotDataSub.getMsg());
                if (team == ObjectConfig.MY_TEAM && ID == 0) {
                    updatePath();
                    publishNextNode();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    public void setData(RobotData data) {
        this.data = data;
    }

    public void setPath(ArrayList<Vec2D> path) {
        this.path = path;
    }

    public ArrayList<Vec2D> getPath() {
        return path;
    }
}