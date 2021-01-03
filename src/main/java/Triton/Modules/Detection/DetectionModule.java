package Triton.Modules.Detection;

import Proto.MessagesRobocupSslDetection.SSL_DetectionFrame;
import Proto.MessagesRobocupSslDetection.SSL_DetectionRobot;
import Triton.Config.ObjectConfig;
import Triton.Dependencies.DesignPattern.PubSubSystem.Module;
import Triton.Dependencies.DesignPattern.PubSubSystem.*;
import Triton.Dependencies.Team;

import java.util.ArrayList;

/**
 * Module to process object detection data from VisionModule
 */
public class DetectionModule implements Module {
    // Data objects
    private final ArrayList<RobotData> yellowRobotsData;
    private final ArrayList<RobotData> blueRobotsData;
    private final BallData ball;

    // Publishers
    private final Subscriber<SSL_DetectionFrame> detectSub;
    private final ArrayList<Publisher<RobotData>> yellowRobotPubs;
    private final ArrayList<Publisher<RobotData>> blueRobotPubs;
    private final Publisher<BallData> ballPub;

    /**
     * Constructs a DetectionModule
     */
    public DetectionModule() {
        detectSub = new MQSubscriber<>("vision", "detection", 10);

        yellowRobotsData = new ArrayList<>();
        blueRobotsData = new ArrayList<>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobotsData.add(new RobotData(Team.YELLOW, i));
            blueRobotsData.add(new RobotData(Team.BLUE, i));
        }

        yellowRobotPubs = new ArrayList<>();
        blueRobotPubs = new ArrayList<>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobotPubs
                    .add(new FieldPublisher<>("detection", "yellow robot data" + i, yellowRobotsData.get(i)));
            blueRobotPubs.add(new FieldPublisher<>("detection", "blue robot data" + i, blueRobotsData.get(i)));
        }

        ball = new BallData();
        ballPub = new FieldPublisher<>("detection", "ball", ball);
    }

    /**
     * Repeatedly updates detection data
     */
    public void run() {
        try {
            subscribe();

            while (true) {
                update(detectSub.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Subscribe to publishers
     */
    private void subscribe() {
        try {
            detectSub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates data and publish to subscribers
     *
     * @param frame SSL_Detection frame, sent from VisionModule
     */
    public void update(SSL_DetectionFrame frame) {
        double time = frame.getTCapture();

        for (SSL_DetectionRobot robotFrame : frame.getRobotsYellowList()) {
            int id = robotFrame.getRobotId();
            yellowRobotsData.get(id).update(robotFrame, time);
            yellowRobotPubs.get(id).publish(yellowRobotsData.get(id));
        }

        for (SSL_DetectionRobot robotFrame : frame.getRobotsBlueList()) {
            int id = robotFrame.getRobotId();
            blueRobotsData.get(id).update(robotFrame, time);
            blueRobotPubs.get(id).publish(blueRobotsData.get(id));
        }

        if (frame.getBallsCount() > 0)
            ball.update(frame.getBalls(0), time);
        ballPub.publish(ball);
    }
}