package triton.manualTests.coreTests.estimatorTests;

import triton.App;
import triton.config.Config;
import triton.config.globalVariblesAndConstants.GvcModuleFreqs;
import triton.coreModules.ai.estimators.AttackSupportMapModule;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.RobotList;
import triton.manualTests.TritonTestable;
import triton.periphModules.display.Display;
import triton.periphModules.display.PaintOption;
import triton.Util;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static triton.periphModules.display.PaintOption.*;
import static triton.Util.delay;

public class AttackSupportMapTest implements TritonTestable {

    AttackSupportMapModule atkSupportMap;
    RobotList<Ally> fielders;

    public AttackSupportMapTest(RobotList<Ally> fielders, RobotList<Foe> foes, Ball ball) {
        this.fielders = fielders;
        atkSupportMap = new AttackSupportMapModule(fielders, foes, ball);
        atkSupportMap.run();
    }

    public boolean test(Config config) {
        fielders.stopAll();
        Display display = new Display(config);
        ArrayList<PaintOption> paintOptions = new ArrayList<>();
        paintOptions.add(GEOMETRY);
        paintOptions.add(OBJECTS);
        paintOptions.add(INFO);
        paintOptions.add(PROBABILITY);
        display.setPaintOptions(paintOptions);
        display.setProbFinder(atkSupportMap);

        ScheduledFuture<?> displayFuture = App.threadPool.scheduleAtFixedRate(display,
                0,
                Util.toPeriod(GvcModuleFreqs.DISPLAY_MODULE_FREQ, TimeUnit.NANOSECONDS),
                TimeUnit.NANOSECONDS);
        Scanner scanner = new Scanner(System.in);

        RobotList<Ally> decoys = new RobotList<>();
        decoys.add(fielders.get(4));
        decoys.add(fielders.get(5));
        decoys.add(fielders.get(3));
        atkSupportMap.setDecoys(decoys);

        while(true) {
            atkSupportMap.setDecoys(decoys);
            delay(10);
        }

//        while(true) {
//
//            double[][] eval = atkSupportMap.getEval();
//            System.out.println(Arrays.deepToString(eval));
//
//            try {
//                Thread.sleep(1);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

    }
}
