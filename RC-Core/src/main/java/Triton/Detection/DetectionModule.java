package Triton.Detection;

import Triton.Config.ObjectConfig;
import Triton.DesignPattern.PubSubSystem.Module;
import Triton.DesignPattern.PubSubSystem.Publisher;
import Triton.DesignPattern.PubSubSystem.Subscriber;
import java.util.HashMap;
import Proto.MessagesRobocupSslDetection.SSL_DetectionFrame;
import Proto.MessagesRobocupSslDetection.SSL_DetectionRobot;

public class DetectionModule implements Module {

    private HashMap<Team, HashMap<Integer, Robot>> robotMap;
    private Ball ball;

    private Subscriber<SSL_DetectionFrame> detectSub;
    private Publisher<HashMap<Team, HashMap<Integer, Robot>>> robotPub;
    private Publisher<Ball> ballPub;

    public DetectionModule() {
        detectSub = new Subscriber<SSL_DetectionFrame>("vision", "detection", 10);
        robotPub = new Publisher<HashMap<Team, HashMap<Integer, Robot>>>("detection", "robot");
        ballPub = new Publisher<Ball>("detection", "ball");

        ball = new Ball();

        robotMap = new HashMap<Team, HashMap<Integer, Robot>>();
        HashMap<Integer, Robot> yellowRobots = new HashMap<Integer, Robot>();
        HashMap<Integer, Robot> blueRobots = new HashMap<Integer, Robot>();
        robotMap.put(Team.YELLOW, yellowRobots);
        robotMap.put(Team.BLUE, blueRobots);
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            robotMap.get(Team.YELLOW).put(i, new Robot(Team.YELLOW, i));
            robotMap.get(Team.BLUE).put(i, new Robot(Team.BLUE, i));
        }
    }

    public void run() {
        while (!detectSub.subscribe());

        while (true) {
            try {
                update(detectSub.pollMsg());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void update(SSL_DetectionFrame df) {
        double time = df.getTCapture();

        for (SSL_DetectionRobot r : df.getRobotsYellowList()) {
            if (r.getRobotId() < ObjectConfig.ROBOT_COUNT) {
                robotMap.get(Team.YELLOW).get(r.getRobotId()).update(r, time);
            }
        }

        for (SSL_DetectionRobot r : df.getRobotsBlueList()) {
            if (r.getRobotId() < ObjectConfig.ROBOT_COUNT)
                robotMap.get(Team.BLUE).get(r.getRobotId()).update(r, time);
        }

        robotPub.publish(robotMap);

        if (df.getBallsCount() > 0)
            ball.update(df.getBalls(0), time);
        ballPub.publish(ball);
    }
}