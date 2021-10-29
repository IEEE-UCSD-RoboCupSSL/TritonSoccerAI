package triton.manualTests.periphMiscTests;

import triton.App;
import triton.config.Config;
import triton.config.globalVariblesAndConstants.GvcModuleFreqs;
import triton.coreModules.robot.Team;
import triton.manualTests.TritonTestable;
import triton.misc.modulePubSubSystem.FieldSubscriber;
import triton.misc.modulePubSubSystem.Subscriber;
import triton.periphModules.detection.DetectionModule;
import triton.periphModules.detection.RobotData;
import triton.periphModules.vision.ERForceVisionModule;
import triton.Util;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static triton.Util.delay;


public class DetectionTest_OldProto implements TritonTestable {
    private ArrayList<Subscriber<RobotData>> yellowRobotSubs;
    private ArrayList<Subscriber<RobotData>> blueRobotSubs;

    public boolean test(Config config) {
        ERForceVisionModule erForceSimVisionModule = new ERForceVisionModule(config);
        App.threadPool.scheduleAtFixedRate(
                erForceSimVisionModule,
            0, Util.toPeriod(GvcModuleFreqs.VISION_MODULE_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        App.runModule(new DetectionModule(config), GvcModuleFreqs.DETECTION_MODULE_FREQ);

        yellowRobotSubs = new ArrayList<>();
        blueRobotSubs = new ArrayList<>();
        for (int i = 0; i < config.numAllyRobots; i++) {
            blueRobotSubs.add(new FieldSubscriber<>("From:DetectionModule", Team.BLUE.name() + i));
            yellowRobotSubs.add(new FieldSubscriber<>("From:DetectionModule", Team.YELLOW.name() + i));
        }

        try {
            for (int i = 0; i < config.numAllyRobots; i++) {
                blueRobotSubs.get(i).subscribe(1000);
                yellowRobotSubs.get(i).subscribe(1000);
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        while (true) {
            for (int i = 0; i < config.numAllyRobots; i++) {
                RobotData blueData = blueRobotSubs.get(i).getMsg();
                RobotData yellowData = yellowRobotSubs.get(i).getMsg();
                System.out.println("BLUE " + blueData.getID() + ": " + blueData.getPos());

                System.out.println("YELLOW " + yellowData.getID() + ": " + yellowData.getPos());
            }
            delay(3);
        }
    }
}
