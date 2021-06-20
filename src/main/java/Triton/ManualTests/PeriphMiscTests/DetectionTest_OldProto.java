package Triton.ManualTests.PeriphMiscTests;

import Triton.App;
import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.CoreModules.Robot.Team;
import Triton.ManualTests.TritonTestable;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Subscriber;
import Triton.PeriphModules.Detection.DetectionModule;
import Triton.PeriphModules.Detection.RobotData;
import Triton.PeriphModules.Vision.ERForceVisionModule;
import Triton.Util;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static Triton.Util.delay;


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
