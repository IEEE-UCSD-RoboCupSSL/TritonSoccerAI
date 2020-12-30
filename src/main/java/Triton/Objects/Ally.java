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
    protected ThreadPoolExecutor pool;
    private final Subscriber<MessagesRobocupSslGeometry.SSL_GeometryFieldSize> fieldSizeSub;
    private final ArrayList<Subscriber<RobotData>> yellowRobotSubs;
    private final ArrayList<Subscriber<RobotData>> blueRobotSubs;
    private final Publisher<RemoteAPI.Commands> commandsPub;

    private PathFinder pathFinder;
    private ArrayList<Vec2D> path;
    private double angle;
    private Vec2D kickVel;

    private final Publisher<Vec2D> endPointPub;
    private final Subscriber<Vec2D> endPointSub;
    private final Publisher<Double> anglePub;
    private final Subscriber<Double> angleSub;
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

        endPointPub = new FieldPublisher<>("Ally endPoint", "" + ID, null);
        endPointSub = new FieldSubscriber<>("Ally endPoint", "" + ID);

        anglePub = new FieldPublisher<>("Ally angle", "" + ID, null);
        angleSub = new FieldSubscriber<>("Ally angle", "" + ID);

        kickVelPub = new FieldPublisher<>("Ally kickVel", "" + ID, null);
        kickVelSub = new FieldSubscriber<>("Ally kickVel", "" + ID);

        commandsPub = new MQPublisher<>("commands", "" + ID);

        conn.buildTcpConnection();
        conn.buildCommandUDP();
        // conn.buildVisionStream(ip, port + ConnectionConfig.VISION_UDP_OFFSET);
        // conn.buildDataStream(port + ConnectionConfig.DATA_UDP_OFFSET);
    }

    public void setVel() {
    }

    public void setAngVel() {
    }

    // runs in the caller thread
    public void moveTo(Vec2D endPoint) {
        endPointPub.publish(endPoint);
    }

    public void rotateTo(double angle) {
        anglePub.publish(angle);
    }

    // runs in the caller thread
    public void kick(Vec2D kickVel) {
        kickVelPub.publish(kickVel);
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
                updateAngle();
                updateKick();
                publishNextCommand();
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
            angleSub.subscribe();
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
        Vec2D endPoint = endPointSub.getMsg();
        if (endPoint == null)
            return;

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

    private void updateAngle() {
        angle = angleSub.getMsg();
    }

    private void updateKick() {
        Vec2D kickVel = kickVelSub.getMsg();
        if (kickVel != null)
            this.kickVel = kickVel;
    }

    /**
     * Publishes the next node in the set path of the robot
     */
    private void publishNextCommand() {
        RemoteAPI.Commands.Builder command = RemoteAPI.Commands.newBuilder();
        command.setIsWorldFrame(true);

        command.setEnableBallAutoCapture(false);

        RemoteAPI.Vec3D.Builder motionSetPoint = RemoteAPI.Vec3D.newBuilder();
        if (path != null && path.size() > 1) {
            Vec2D nextNode = path.get(1);
            command.setMode(path.size() > 2 ? 4 : 0);
            motionSetPoint.setX(-nextNode.y);
            motionSetPoint.setY(nextNode.x);
        } else {
            command.setMode(0);
            motionSetPoint.setX(0);
            motionSetPoint.setY(0);
        }
        motionSetPoint.setZ(angle);
        command.setMotionSetPoint(motionSetPoint);

        RemoteAPI.Vec2D.Builder kickerSetPoint = RemoteAPI.Vec2D.newBuilder();
        if (kickVel != null) {
            kickerSetPoint.setX(kickVel.x);
            kickerSetPoint.setY(kickVel.y);
        } else {
            kickerSetPoint.setX(0);
            kickerSetPoint.setY(0);
        }
        command.setKickerSetPoint(kickerSetPoint);

        commandsPub.publish(command.build());
    }
}