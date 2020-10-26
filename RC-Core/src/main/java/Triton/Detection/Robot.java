package Triton.Detection;

import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

import org.javatuples.Pair;

import Proto.MessagesRobocupSslGeometry.SSL_GeometryFieldSize;
import Triton.Computation.PathFinder.PathFinder;
import Triton.Computation.PathFinder.PathRelayer;
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

    private Publisher<Pair<ArrayList<Vec2D>, Double>> pathPub;
    private PathRelayer pathRelayer;

    public Robot(Team team, int ID, ThreadPoolExecutor pool) {
        this.team = team;
        this.ID = ID;
        this.pool = pool;

        conn = new RobotConnection(team, ID);

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

        conn.buildTcpConnection(ip, port);
        conn.buildCommandUDP(ip, port);
        conn.buildDataStream(ip, port);
        conn.buildVisionStream(ip, port);

        String name = (team == Team.YELLOW) ? "yellow robot data" + ID : "blue robot data" + ID;
        robotDataSub = new FieldSubscriber<RobotData>("detection", name);
        fieldSizeSub = new FieldSubscriber<SSL_GeometryFieldSize>("geometry", "fieldSize");

        yellowRobotSubs = new ArrayList<Subscriber<RobotData>>();
        blueRobotSubs = new ArrayList<Subscriber<RobotData>>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobotSubs.add(new FieldSubscriber<RobotData>("detection", "yellow robot data" + i));
            blueRobotSubs.add(new FieldSubscriber<RobotData>("detection", "blue robot data" + i));
        }

        pathPub = new MQPublisher<Pair<ArrayList<Vec2D>, Double>>("path commands", team.name() + ID);
        pathRelayer = new PathRelayer(team, ID, pool);
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

    public void setEndPoint(Vec2D endPoint, double angle) {
        if (pathFinder == null) {
            fieldSizeSub.subscribe();
            while (true) {
                SSL_GeometryFieldSize fieldSize = fieldSizeSub.getMsg();

                if (fieldSize == null || fieldSize.getFieldLength() == 0 || fieldSize.getFieldWidth() == 0
                        || fieldSize.getGoalDepth() == 0)
                    continue;

                double worldSizeX = fieldSize.getFieldLength();
                double worldSizeY = fieldSize.getFieldWidth();

                pathFinder = new JPSPathFinder(worldSizeX, worldSizeY);
            }
        }

        pathFinder.setObstacles(getObstacles());

        ArrayList<Vec2D> path = pathFinder.findPath(data.getPos(), endPoint);
        Pair<ArrayList<Vec2D>, Double> pathWithEndDir = new Pair<ArrayList<Vec2D>, Double>(path, angle);
        pathPub.publish(pathWithEndDir);
    }

    private ArrayList<Circle2D> getObstacles() {
        for (Subscriber<RobotData> robotSub : yellowRobotSubs) {
            robotSub.subscribe();
        }
        for (Subscriber<RobotData> robotSub : blueRobotSubs) {
            robotSub.subscribe();
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
        robotDataSub.subscribe();

        pool.execute(conn.getTCPConnection());
        pool.execute(conn.getCommandStream());
        pool.execute(conn.getVisionStream());
        pool.execute(conn.getDataStream());

        while (true) {
            setData(robotDataSub.getMsg());
        }
    }

    public void setData(RobotData data) {
        this.data = data;
    }
}
