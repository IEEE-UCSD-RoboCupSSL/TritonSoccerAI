package Triton.PeriphModules.Detection;

import Proto.SslVisionDetection;
import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcGeneral;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.Misc.ModulePubSubSystem.*;

import java.util.ArrayList;

/**
 * Module to process object detection data from VisionModule
 */
public class DetectionModule implements Module {

    private final Config config;

    // Data objects
    private final ArrayList<RobotData> yellowRobotsData;
    private final ArrayList<RobotData> blueRobotsData;
    private final BallData ballData;

    // Subscribers
    private final Subscriber<Triton.Legacy.OldGrSimProto.protosrcs.MessagesRobocupSslDetection.SSL_DetectionFrame> visionSub_OldProto;
    private final Subscriber<SslVisionDetection.SSL_DetectionFrame> visionSub;

    // Publishers
    private final ArrayList<Publisher<RobotData>> yellowRobotPubs;
    private final ArrayList<Publisher<RobotData>> blueRobotPubs;
    private final Publisher<BallData> ballPub;

    private boolean isFirstRun = true;

    /**
     * Constructs a DetectionModule
     */
    public DetectionModule(Config config) {
        this.config = config;
        if(config.cliConfig.simulator == GvcGeneral.SimulatorName.ErForceSim) {
            visionSub = new MQSubscriber<>("From:ERForceVisionModule", "Detection");
            visionSub_OldProto = null;
        } else if(config.cliConfig.simulator == GvcGeneral.SimulatorName.GrSim) {
            visionSub_OldProto = new MQSubscriber<>("From:GrSimVisionModule_OldProto", "Detection");
            visionSub = null;
        } else {
            visionSub_OldProto = null;
            visionSub = null;
        }
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
        if(config.cliConfig.simulator == GvcGeneral.SimulatorName.GrSim) {
            update(visionSub_OldProto.getMsg());
        }
        if(config.cliConfig.simulator == GvcGeneral.SimulatorName.ErForceSim) {
            update(visionSub.getMsg());
        }
    }

    /**
     * Subscribe to publishers
     */
    private void subscribe() {
        try {
            if(config.cliConfig.simulator == GvcGeneral.SimulatorName.GrSim) {
                visionSub_OldProto.subscribe(1000);
            }
            if(config.cliConfig.simulator == GvcGeneral.SimulatorName.ErForceSim) {
                visionSub.subscribe(1000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates data and publish to subscribers
     *
     * @param frame SSL_Detection frame, sent from VisionModule
     */
    public void update(Triton.Legacy.OldGrSimProto.protosrcs.MessagesRobocupSslDetection.SSL_DetectionFrame frame) {
        if (frame == null) return;
        double time = frame.getTCapture();

        for (Triton.Legacy.OldGrSimProto.protosrcs.MessagesRobocupSslDetection.SSL_DetectionRobot robotFrame : frame.getRobotsBlueList()) {
            int id = robotFrame.getRobotId();
            blueRobotsData.get(id).update(robotFrame, time);
            blueRobotPubs.get(id).publish(blueRobotsData.get(id));
        }

        for (Triton.Legacy.OldGrSimProto.protosrcs.MessagesRobocupSslDetection.SSL_DetectionRobot robotFrame : frame.getRobotsYellowList()) {
            int id = robotFrame.getRobotId();
            yellowRobotsData.get(id).update(robotFrame, time);
            yellowRobotPubs.get(id).publish(yellowRobotsData.get(id));
        }

        if (frame.getBallsCount() > 0)
            ballData.update(frame.getBalls(0), time);
        ballPub.publish(ballData);
    }


    /**
     * Updates data and publish to subscribers
     *
     * @param frame SSL_Detection frame, sent from VisionModule
     */
    public void update(SslVisionDetection.SSL_DetectionFrame frame) {
        if (frame == null) return;
        double time = frame.getTCapture();

        for (SslVisionDetection.SSL_DetectionRobot robotFrame : frame.getRobotsBlueList()) {
            int id = robotFrame.getRobotId();
            blueRobotsData.get(id).update(robotFrame, time, config);
            blueRobotPubs.get(id).publish(blueRobotsData.get(id));
        }

        for (SslVisionDetection.SSL_DetectionRobot robotFrame : frame.getRobotsYellowList()) {
            int id = robotFrame.getRobotId();
            yellowRobotsData.get(id).update(robotFrame, time, config);
            yellowRobotPubs.get(id).publish(yellowRobotsData.get(id));
        }

        if (frame.getBallsCount() > 0)
            ballData.update(frame.getBalls(0), time, config);
        ballPub.publish(ballData);
    }
}