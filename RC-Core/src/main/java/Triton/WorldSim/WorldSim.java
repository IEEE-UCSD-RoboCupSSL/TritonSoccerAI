package Triton.WorldSim;

import java.util.Collection;
import java.util.HashMap;

import Proto.MessagesRobocupSslDetection.SSL_DetectionFrame;
import Triton.Config.ObjectConfig;
import Triton.Detection.Ball;
import Triton.Detection.DetectionData;
import Triton.Detection.Robot;
import Triton.Detection.Team;
import Triton.Geometry.Field;
import Triton.Geometry.GeometryData;
import Triton.Shape.Vec2D;

public class WorldSim {
    private double worldSizeX, worldSizeY;
    private double time;
    private double leftBound, rightBound, botBound, topBound;
    private HashMap<Team, HashMap<Integer, RobotSim>> robots;
    private BallSim ball;

    public WorldSim() {
        Field field = GeometryData.get().getField();
        worldSizeX = field.fieldLength;
        worldSizeY = field.fieldWidth;
        leftBound = -worldSizeX / 2;
        rightBound = worldSizeX / 2;
        botBound = -worldSizeY / 2;
        topBound = worldSizeY / 2;

        robots = new HashMap<Team, HashMap<Integer, RobotSim>>();
        robots.put(Team.BLUE, new HashMap<Integer, RobotSim>());
        robots.put(Team.YELLOW, new HashMap<Integer, RobotSim>());
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            robots.get(Team.BLUE).put(i, new RobotSim(Team.BLUE, i, new Vec2D(0, 0), new Vec2D(0, 0), 0, 0));
            robots.get(Team.YELLOW).put(i, new RobotSim(Team.YELLOW, i, new Vec2D(0, 0), new Vec2D(0, 0), 0, 0));
        }
    }

    public void transfer(DetectionData detect) {
        double worldTime = DetectionData.get().getTime();
        setTime(worldTime);

        HashMap<Team, HashMap<Integer, Robot>> detectionRobots = detect.getRobots();
        for (Robot robot : detectionRobots.get(Team.BLUE).values())
            transferRobot(robot);
        for (Robot robot : detectionRobots.get(Team.YELLOW).values())
            transferRobot(robot);

        Ball detectionBall = detect.getBall();
        ball.setPos(detectionBall.getPos());
        ball.setPos(detectionBall.getVel());
    }

    private void transferRobot(Robot detectionRobot) {
        RobotSim robotSim = getRobot(detectionRobot.getTeam(), detectionRobot.getID());
        robotSim.setPos(detectionRobot.getPos());
        robotSim.setVel(detectionRobot.getVel());
        robotSim.setOrient(detectionRobot.getOrient());
        robotSim.setAngleVel(detectionRobot.getAngularVelocity());
    }

    public void update(double delta) {
        setTime(time + delta);
        updateRobots(robots.get(Team.BLUE).values(), delta);
        updateRobots(robots.get(Team.YELLOW).values(), delta);
    }

    private void updateRobots(Collection<RobotSim> robots, double delta) {
        for (RobotSim robot : robots) {
            robot.update(delta);

            if (robot.getPos().x < leftBound) {
                robot.setPos(new Vec2D(leftBound, robot.getPos().y));
                robot.setVel(new Vec2D(0, robot.getVel().y));
            } else if (robot.getPos().x > rightBound) {
                robot.setPos(new Vec2D(rightBound, robot.getPos().y));
                robot.setVel(new Vec2D(0, robot.getVel().y));
            }

            if (robot.getPos().y < botBound) {
                robot.setPos(new Vec2D(robot.getPos().x, botBound));
                robot.setVel(new Vec2D(robot.getVel().x, 0));
            } else if (robot.getPos().x > topBound) {
                robot.setPos(new Vec2D(robot.getPos().x, topBound));
                robot.setVel(new Vec2D(robot.getVel().x, 0));
            }
        }
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public int getBallsCount() {
        return 1;
    }

    public HashMap<Team, HashMap<Integer, RobotSim>> getRobots() {
        return robots;
    }

    public RobotSim getRobot(Team team, int ID) {
        return robots.get(team).get(ID);
    }

    public BallSim getBall() {
        return ball;
    }
}
