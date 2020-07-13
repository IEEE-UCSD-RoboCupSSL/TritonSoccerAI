package Triton.Detection;

import java.util.HashMap;
import Triton.Geometry.Point2D;
import Proto.MessagesRobocupSslDetection.SSL_DetectionFrame;
import Proto.MessagesRobocupSslDetection.SSL_DetectionRobot;

public class DetectionManager {

    public static final int ROBOT_COUNT = 6;

    public HashMap<Team, HashMap<Integer, Robot>> robots;
    private int ballCount;
    private Ball ball;

    public DetectionManager() {
        robots = new HashMap<>();
        robots.put(Team.YELLOW, new HashMap<Integer, Robot>());
        robots.put(Team.BLUE,   new HashMap<Integer, Robot>());

        for (int i = 0; i < ROBOT_COUNT; i++) {
            robots.get(Team.YELLOW).put(i, new Robot(Team.YELLOW, i));
            robots.get(Team.BLUE).put(i, new Robot(Team.BLUE, i));
        }
        ball = new Ball();
    }

    public void update(SSL_DetectionFrame df) {
        double time = df.getTCapture();

        for (SSL_DetectionRobot r : df.getRobotsYellowList()) {
            getRobot(Team.YELLOW, r.getRobotId()).update(r, time);
        }
        for (SSL_DetectionRobot r : df.getRobotsBlueList()) {
            getRobot(Team.BLUE, r.getRobotId()).update(r, time);
        }
        ballCount = df.getBallsCount();
        if (ballCount > 0) {
            ball.update(df.getBalls(0), time);
        }
    }

    public int getBallCount() {
        return ballCount;
    }
    
    public Ball getBall() {
        return ball;
    }

    public Point2D getBallPos() {
        return ball.getPos();
    }

    public Robot getRobot(Team team, int ID) {
        return robots.get(team).get(ID);
    }

    public Point2D getRobotPos(Team team, int ID) {
        return getRobot(team, ID).getPos();
    }

    public double getRobotOrient(Team team, int ID) {
        return getRobot(team, ID).getOrient();
    }
}