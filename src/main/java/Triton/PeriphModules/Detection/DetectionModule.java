package Triton.PeriphModules.Detection;

import Triton.Config.Config;
import Triton.Legacy.OldGrSimProto.protosrcs.MessagesRobocupSslDetection.SSL_DetectionFrame;
import Triton.Legacy.OldGrSimProto.protosrcs.MessagesRobocupSslDetection.SSL_DetectionRobot;
import Triton.Config.OldConfigs.ObjectConfig;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.Misc.ModulePubSubSystem.*;

import java.util.ArrayList;

/**
 * Module to process object detection data from VisionModule
 */
public class DetectionModule implements Module {
    // Data objects
    private final ArrayList<RobotData> yellowRobotsData;
    private final ArrayList<RobotData> blueRobotsData;
    private final BallData ballData;

    // Subscribers
    private final Subscriber<SSL_DetectionFrame> visionSub;

    // Publishers
    private final ArrayList<Publisher<RobotData>> yellowRobotPubs;
    private final ArrayList<Publisher<RobotData>> blueRobotPubs;
    private final Publisher<BallData> ballPub;

    private boolean isFirstRun = true;

    /**
     * Constructs a DetectionModule
     */
    public DetectionModule(Config config) {
        visionSub = new MQSubscriber<>("vision", "detection");

        yellowRobotsData = new ArrayList<>();
        blueRobotsData = new ArrayList<>();
        for (int i = 0; i < config.connConfig.numRobots; i++) {
            yellowRobotsData.add(new RobotData(Team.YELLOW, i));
            blueRobotsData.add(new RobotData(Team.BLUE, i));
        }

        yellowRobotPubs = new ArrayList<>();
        blueRobotPubs = new ArrayList<>();
        for (int i = 0; i < config.connConfig.numRobots; i++) {
            blueRobotPubs.add(new FieldPublisher<>("detection", Team.BLUE.name() + i, blueRobotsData.get(i)));
            yellowRobotPubs.add(new FieldPublisher<>("detection", Team.YELLOW.name() + i, yellowRobotsData.get(i)));
        }

        ballData = new BallData();
        ballPub = new FieldPublisher<>("detection", "ball", ballData);
    }

    /**
     * Repeatedly updates detection data
     */
    public void run() {
        if (isFirstRun) {
            try {
                subscribe();
            } catch (Exception e) {
                e.printStackTrace();
            }

            isFirstRun = false;
        }

        update(visionSub.getMsg());
    }

    /**
     * Subscribe to publishers
     */
    private void subscribe() {
        try {
            visionSub.subscribe(1000);
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

        for (SSL_DetectionRobot robotFrame : frame.getRobotsBlueList()) {
            int id = robotFrame.getRobotId();
            blueRobotsData.get(id).update(robotFrame, time);
            blueRobotPubs.get(id).publish(blueRobotsData.get(id));
        }

        for (SSL_DetectionRobot robotFrame : frame.getRobotsYellowList()) {
            int id = robotFrame.getRobotId();
            yellowRobotsData.get(id).update(robotFrame, time);
            yellowRobotPubs.get(id).publish(yellowRobotsData.get(id));
        }

        if (frame.getBallsCount() > 0)
            ballData.update(frame.getBalls(0), time);
        ballPub.publish(ballData);
    }
}