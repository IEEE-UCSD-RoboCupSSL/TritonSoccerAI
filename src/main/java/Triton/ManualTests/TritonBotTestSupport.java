package Triton.ManualTests;

import Triton.App;
import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.ManualTests.RobotSkillsTests.PrimitiveMotionTest;
import Triton.PeriphModules.Detection.DetectionModule;
import Triton.PeriphModules.Vision.GrSimVisionModule_OldProto;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;

import static Triton.Util.delay;

public class TritonBotTestSupport {

    private static void runPrimitiveMotionTest(Config config, int id) {
        ArrayList<ScheduledFuture<?>> moduleFutures = new ArrayList<>();
        Ally bot;

        System.out.println("Test Started!");
        moduleFutures.add(App.runModule(new GrSimVisionModule_OldProto(config), GvcModuleFreqs.VISION_MODULE_FREQ));
        moduleFutures.add(App.runModule(new DetectionModule(config), GvcModuleFreqs.DETECTION_MODULE_FREQ));
        delay(1000);


        bot = new Ally(config, id);
        bot.connect();
        App.runModule(bot, GvcModuleFreqs.ROBOT_FREQ);
        delay(1500);

        new PrimitiveMotionTest(bot).test(config);
    }

    private static void runPrintHoldBall(Config config, int id) {
        ArrayList<ScheduledFuture<?>> moduleFutures = new ArrayList<>();
        Ally bot;

        System.out.println("Test Started!");
        moduleFutures.add(App.runModule(new GrSimVisionModule_OldProto(config), GvcModuleFreqs.VISION_MODULE_FREQ));
        moduleFutures.add(App.runModule(new DetectionModule(config), GvcModuleFreqs.DETECTION_MODULE_FREQ));
        delay(1000);


        bot = new Ally(config, id);
        bot.connect();
        App.runModule(bot, GvcModuleFreqs.ROBOT_FREQ);
        delay(1500);
        System.out.println("This test will quit in 5 seconds");
        long t0 = System.currentTimeMillis();
        while(System.currentTimeMillis() - t0 < 5000) {
            System.out.println(bot.isHoldingBall());
            delay(100);
        }
    }


}
