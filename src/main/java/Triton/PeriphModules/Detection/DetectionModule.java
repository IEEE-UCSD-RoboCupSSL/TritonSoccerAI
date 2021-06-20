package Triton.PeriphModules.Detection;

import Triton.Config.Config;
import Triton.Legacy.OldGrSimProto.protosrcs.MessagesRobocupSslDetection.SSL_DetectionFrame;
import Triton.Legacy.OldGrSimProto.protosrcs.MessagesRobocupSslDetection.SSL_DetectionRobot;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.Misc.ModulePubSubSystem.*;
import io.grpc.netty.shaded.io.netty.internal.tcnative.SSL;

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
        visionSub = new FieldSubscriber<>("From:ERForceVisionModule", "Detection");

        yellowRobotsData = new ArrayList<>();
        blueRobotsData = new ArrayList<>();
        for (int i = 0; i < config.numAllyRobots; i++) {
            yellowRobotsData.add(new RobotData(Team.YELLOW, i));
            blueRobotsData.add(new RobotData(Team.BLUE, i));
        }

        yellowRobotPubs = new ArrayList<>();
        blueRobotPubs = new ArrayList<>();
        for (int i = 0; i < config.numAllyRobots; i++) {
            blueRobotPubs.add(new FieldPublisher<>("From:DetectionModule", Team.BLUE.name() + i, blueRobotsData.get(i)));
            yellowRobotPubs.add(new FieldPublisher<>("From:DetectionModule", Team.YELLOW.name() + i, yellowRobotsData.get(i)));
        }

        ballData = new BallData();
        ballPub = new FieldPublisher<>("From:DetectionModule", "Ball", ballData);
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

        SSL_DetectionFrame frame = visionSub.getMsg();
        System.out.println("subscribe: " + frame);
        update(frame);
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
        if (frame == null) return;
        double time = frame.getTCapture();

        for (SSL_DetectionRobot robotFrame : frame.getRobotsBlueList()) {
            int id = robotFrame.getRobotId();
            blueRobotsData.get(id).update(robotFrame, time);
            if (id == 0) {
                System.out.println(blueRobotsData.get(id).getPos());
            }
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