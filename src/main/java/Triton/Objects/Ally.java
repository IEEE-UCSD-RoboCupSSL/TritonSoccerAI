package Triton.Objects;

import Proto.MessagesRobocupSslGeometry;
import Proto.RemoteAPI;
import Triton.Algorithms.PathFinder.JPS.JPSPathFinder;
import Triton.Algorithms.PathFinder.PathFinder;
import Triton.Config.ObjectConfig;
import Triton.Dependencies.DesignPattern.PubSubSystem.*;
import Triton.Dependencies.Shape.Circle2D;
import Triton.Dependencies.Shape.Vec2D;
import Triton.Modules.Detection.RobotData;
import Triton.Modules.Detection.Team;
import Triton.Modules.RemoteStation.RobotConnection;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;


public class Ally extends Robot {
    private final RobotConnection conn;
    private final Subscriber<MessagesRobocupSslGeometry.SSL_GeometryFieldSize> fieldSizeSub;
    private final ArrayList<Subscriber<RobotData>> yellowRobotSubs;
    private final ArrayList<Subscriber<RobotData>> blueRobotSubs;
    private final Subscriber<Pair<Vec2D, Double>> endPointSub;
    protected ThreadPoolExecutor pool;
    private PathFinder pathFinder;
    private Publisher<RemoteAPI.Commands> commandsPub;

    private ArrayList<Vec2D> path;
    private double angle;

    private Publisher<Pair<Vec2D, Double>> endPointPub;

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

        endPointPub = new FieldPublisher<>("endPoint", "" + ID, null);
        endPointSub = new FieldSubscriber<>("endPoint", "" + ID);

        commandsPub = new MQPublisher<>("commands", "" + ID);

        conn.buildTcpConnection();
        conn.buildCommandUDP();
        // conn.buildVisionStream(ip, port + ConnectionConfig.VISION_UDP_OFFSET);
        // conn.buildDataStream(port + ConnectionConfig.DATA_UDP_OFFSET);
    }

    // runs in the caller thread
    public void moveTo(Vec2D endPoint, double angle) {
        Pair<Vec2D, Double> endPointPair = new Pair<>(endPoint, angle);
        endPointPub.publish(endPointPair);
    }

    public void getBall() {
    }

    public void intercept() {
    }

    public void pass() {
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
            // pool.execute(conn.getVisionStream());
            // pool.execute(conn.getDataStream());

            while (true) {
                updatePath();
                publishNextNode();
            }
        } catch (Exception e) {
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
            endPointSub.subscribe();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize pathfinder with geometry information
     */
    private void initPathfinder() {
        while (pathFinder == null) {
            MessagesRobocupSslGeometry.SSL_GeometryFieldSize fieldSize = fieldSizeSub.getMsg();

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
    private void updatePath() {
        Pair<Vec2D, Double> endPointPair = endPointSub.getMsg();
        if (endPointPair == null)
            return;

        Vec2D endPoint = endPointPair.getValue0();
        angle = endPointPair.getValue1();

        pathFinder.setObstacles(getObstacles());
        ArrayList<Vec2D> newPath = pathFinder.findPath(getData().getPos(), endPoint);
        if (newPath != null)
            path = newPath;
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

    /**
     * Publishes the next node in the set path of the robot
     */
    private void publishNextNode() {
        if (path == null || path.size() <= 1)
            return;

        Vec2D nextNode = path.get(1);
        RemoteAPI.Commands.Builder command = RemoteAPI.Commands.newBuilder();

        if (path.size() > 2) {
            command.setMode(4);
        } else {
            command.setMode(0);
        }

        command.setIsWorldFrame(true);

        RemoteAPI.Vec3D.Builder dest = RemoteAPI.Vec3D.newBuilder();
        dest.setX(-nextNode.y);
        dest.setY(nextNode.x);
        dest.setZ(angle);
        command.setMotionSetPoint(dest);
        commandsPub.publish(command.build());
    }
}