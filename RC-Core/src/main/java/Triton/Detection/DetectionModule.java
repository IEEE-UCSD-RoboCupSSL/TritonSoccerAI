package Triton.Detection;

import Triton.Config.ObjectConfig;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.DesignPattern.PubSubSystem.Module;
import Triton.DesignPattern.PubSubSystem.Publisher;
import Triton.DesignPattern.PubSubSystem.Subscriber;
import java.util.HashMap;
import Proto.MessagesRobocupSslDetection.SSL_DetectionFrame;
import Proto.MessagesRobocupSslDetection.SSL_DetectionRobot;

public class DetectionModule implements Module {

    private HashMap<Team, HashMap<Integer, RobotData>> robotMap;
    private BallData ball;

    private Subscriber<SSL_DetectionFrame> detectSub;
    private Publisher<HashMap<Team, HashMap<Integer, RobotData>>> robotPub;
    private Publisher<BallData> ballPub;

    public DetectionModule() {
        detectSub = new MQSubscriber<SSL_DetectionFrame>("vision", "detection", 10);
        robotPub = new FieldPublisher<HashMap<Team, HashMap<Integer, RobotData>>>("detection", "robot", null);
        ballPub = new FieldPublisher<BallData>("detection", "ball", null);

        ball = new BallData();

        robotMap = new HashMap<Team, HashMap<Integer, RobotData>>();
        HashMap<Integer, RobotData> yellowRobots = new HashMap<Integer, RobotData>();
        HashMap<Integer, RobotData> blueRobots = new HashMap<Integer, RobotData>();
        robotMap.put(Team.YELLOW, yellowRobots);
        robotMap.put(Team.BLUE, blueRobots);
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            robotMap.get(Team.YELLOW).put(i, new RobotData(Team.YELLOW, i));
            robotMap.get(Team.BLUE).put(i, new RobotData(Team.BLUE, i));
        }
    }

    public void run() {
        detectSub.subscribe();

        while (true) {
            try {
                update(detectSub.getMsg());
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