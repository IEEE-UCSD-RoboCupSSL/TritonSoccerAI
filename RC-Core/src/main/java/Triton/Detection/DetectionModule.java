package Triton.Detection;

import Triton.Config.ObjectConfig;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.DesignPattern.PubSubSystem.Module;
import Triton.DesignPattern.PubSubSystem.Publisher;
import Triton.DesignPattern.PubSubSystem.Subscriber;

import java.util.*;
import Proto.MessagesRobocupSslDetection.SSL_DetectionFrame;
import Proto.MessagesRobocupSslDetection.SSL_DetectionRobot;

public class DetectionModule implements Module {
    // Data objects
    private ArrayList<RobotData> yellowRobotsData;
    private ArrayList<RobotData> blueRobotsData;
    private BallData ball;

    // Publishers
    private Subscriber<SSL_DetectionFrame> detectSub;
    private ArrayList<Publisher<RobotData>> yellowRobotPubs;
    private ArrayList<Publisher<RobotData>> blueRobotPubs;
    private Publisher<BallData> ballPub;

    public DetectionModule() {
        detectSub = new MQSubscriber<SSL_DetectionFrame>("vision", "detection", 10);

        yellowRobotsData = new ArrayList<RobotData>();
        blueRobotsData = new ArrayList<RobotData>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobotsData.add(new RobotData(Team.YELLOW, i));
            blueRobotsData.add(new RobotData(Team.BLUE, i));
        }
        
        yellowRobotPubs = new ArrayList<Publisher<RobotData>>();
        blueRobotPubs = new ArrayList<Publisher<RobotData>>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobotPubs.add(new FieldPublisher<RobotData>("detection", "yellow robot data" + i, yellowRobotsData.get(i)));
            blueRobotPubs.add(new FieldPublisher<RobotData>("detection", "blue robot data" + i, blueRobotsData.get(i)));
        }

        ball = new BallData();
        ballPub = new FieldPublisher<BallData>("detection", "ball", ball);
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

    public void update(SSL_DetectionFrame frame) {
        double time = frame.getTCapture();

        for (SSL_DetectionRobot robotFrame : frame.getRobotsYellowList()) {
            if (robotFrame.getRobotId() < ObjectConfig.ROBOT_COUNT) {
                int id = robotFrame.getRobotId();
                yellowRobotsData.get(id).update(robotFrame, time);
                yellowRobotPubs.get(id).publish(yellowRobotsData.get(id));
            }
        }

        for (SSL_DetectionRobot robotFrame : frame.getRobotsBlueList()) {
            if (robotFrame.getRobotId() < ObjectConfig.ROBOT_COUNT) {
                int id = robotFrame.getRobotId();
                blueRobotsData.get(id).update(robotFrame, time);
                blueRobotPubs.get(id).publish(blueRobotsData.get(id));
            }
        }

        if (frame.getBallsCount() > 0)
            ball.update(frame.getBalls(0), time);
        ballPub.publish(ball);
    }
}