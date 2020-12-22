package Triton.Detection;

import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

import org.javatuples.Pair;

import Proto.MessagesRobocupSslGeometry.SSL_GeometryFieldSize;
import Proto.RemoteAPI.Commands;
import Proto.RemoteAPI.Vec3D;
import Triton.Computation.PathFinder.PathFinder;
import Triton.Computation.PathFinder.JPS.JPSPathFinder;
import Triton.Config.ConnectionConfig;
import Triton.Config.ObjectConfig;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.DesignPattern.PubSubSystem.Module;
import Triton.RemoteStation.RobotConnection;
import Triton.Shape.Circle2D;
import Triton.Shape.Vec2D;

public class Robot implements Module {
    private Team team;
    private int ID;
    private ThreadPoolExecutor pool;

    private RobotData data;
    private RobotConnection conn;

    private PathFinder pathFinder;

    private Subscriber<RobotData> robotDataSub;
    private Subscriber<SSL_GeometryFieldSize> fieldSizeSub;
    private ArrayList<Subscriber<RobotData>> yellowRobotSubs;
    private ArrayList<Subscriber<RobotData>> blueRobotSubs;

    private Subscriber<Pair<Vec2D, Double>> endPointSub;
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
            case 0:
                ip = ConnectionConfig.ROBOT_0_IP.getValue0();
                port = ConnectionConfig.ROBOT_0_IP.getValue1();
                break;
            case 1:
                ip = ConnectionConfig.ROBOT_1_IP.getValue0();
                port = ConnectionConfig.ROBOT_1_IP.getValue1();
                break;
            case 2:
                ip = ConnectionConfig.ROBOT_2_IP.getValue0();
                port = ConnectionConfig.ROBOT_2_IP.getValue1();
                break;
            case 3:
                ip = ConnectionConfig.ROBOT_3_IP.getValue0();
                port = ConnectionConfig.ROBOT_3_IP.getValue1();
                break;
            case 4:
                ip = ConnectionConfig.ROBOT_4_IP.getValue0();
                port = ConnectionConfig.ROBOT_4_IP.getValue1();
                break;
            case 5:
                ip = ConnectionConfig.ROBOT_5_IP.getValue0();
                port = ConnectionConfig.ROBOT_5_IP.getValue1();
                break;
            default:
                System.out.println("Invalid Robot ID");
        }

        conn = new RobotConnection(ID);

        String name = (team == Team.YELLOW) ? "yellow robot data" + ID : "blue robot data" + ID;
        robotDataSub = new FieldSubscriber<RobotData>("detection", name);
        fieldSizeSub = new FieldSubscriber<SSL_GeometryFieldSize>("geometry", "fieldSize");

        yellowRobotSubs = new ArrayList<Subscriber<RobotData>>();
        blueRobotSubs = new ArrayList<Subscriber<RobotData>>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobotSubs.add(new FieldSubscriber<RobotData>("detection", "yellow robot data" + i));
            blueRobotSubs.add(new FieldSubscriber<RobotData>("detection", "blue robot data" + i));
        }

        endPointSub = new FieldSubscriber<Pair<Vec2D, Double>>("endPoint", "" + ID);

        if (team == ObjectConfig.MY_TEAM && ID == 0) {
            conn.buildTcpConnection(ip, port + ConnectionConfig.TCP_OFFSET);
            conn.buildCommandUDP(ip, port + ConnectionConfig.COMMAND_UDP_OFFSET);
            // conn.buildVisionStream(ip, port + ConnectionConfig.VISION_UDP_OFFSET);
            // conn.buildDataStream(port + ConnectionConfig.DATA_UDP_OFFSET);
            commandsPub = new MQPublisher<Commands>("commands", "" + ID);
            initPathfinder();
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
        try {
            endPointSub.subscribe(1000);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        Pair<Vec2D, Double> endPointPair = endPointSub.getMsg();
        //System.out.println("in updatePath: " + endPointPair);
        Vec2D endPoint = endPointPair.getValue0();
        angle = endPointPair.getValue1();

        pathFinder.setObstacles(getObstacles());
        path = pathFinder.findPath(data.getPos(), endPoint);
    }

    private ArrayList<Circle2D> getObstacles() {
        for (Subscriber<RobotData> robotSub : yellowRobotSubs) {
            try {
                robotSub.subscribe(1000);
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
        for (Subscriber<RobotData> robotSub : blueRobotSubs) {
            try {
                robotSub.subscribe(1000);
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }

        ArrayList<RobotData> blueRobots = new ArrayList<RobotData>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            blueRobots.add(blueRobotSubs.get(i).getMsg());
        }

        ArrayList<RobotData> yellowRobots = new ArrayList<RobotData>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobots.add(yellowRobotSubs.get(i).getMsg());
        }

        ArrayList<Circle2D> obstacles = new ArrayList<Circle2D>();
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

    @Override
    public void run() {
        try {
            robotDataSub.subscribe(1000);
            fieldSizeSub.subscribe(1000);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
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
                publishCTN();
            }
        }
    }

    // CTN = current target node
    private void publishCTN() {
        Vec2D ctn = path.get(1);
        Commands.Builder command = Commands.newBuilder();
        command.setMode(0);
        command.setIsWorldFrame(true);
        Vec3D.Builder dest = Vec3D.newBuilder();
        dest.setX(-ctn.y);
        dest.setY(ctn.x);
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
