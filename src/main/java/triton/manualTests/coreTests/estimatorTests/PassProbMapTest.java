package triton.manualTests.coreTests.estimatorTests;

import triton.App;
import triton.config.Config;
import triton.config.globalVariblesAndConstants.GvcModuleFreqs;
import triton.coreModules.ai.estimators.PassProbMapModule;
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
import static triton.periphModules.display.PaintOption.PROBABILITY;

public class PassProbMapTest implements TritonTestable {

    PassProbMapModule passProbMap;
    RobotList<Ally> fielders;

    public PassProbMapTest(RobotList<Ally> fielders, RobotList<Foe> foes, Ball ball) {
        passProbMap = new PassProbMapModule(fielders, foes, ball, 50, 20);
        passProbMap.run();
        this.fielders = fielders;
    }

    public boolean test(Config config) {
        Display display = new Display(config);
        ArrayList<PaintOption> paintOptions = new ArrayList<>();
        paintOptions.add(GEOMETRY);
        paintOptions.add(OBJECTS);
        paintOptions.add(INFO);
        paintOptions.add(PROBABILITY);
        display.setPaintOptions(paintOptions);
        display.setProbFinder(passProbMap);

        ScheduledFuture<?> displayFuture = App.threadPool.scheduleAtFixedRate(display, 0,
                Util.toPeriod(GvcModuleFreqs.DISPLAY_MODULE_FREQ, TimeUnit.NANOSECONDS),
                TimeUnit.NANOSECONDS);

        this.fielders.stopAll();

        while(true) {
            Scanner scanner = new Scanner(System.in);
            System.out.println(">> ENTER SCORE:");
            String score;
            try {
                score = scanner.next();
            } catch(Exception e) {
                System.out.println(">> QUIT");
                return true;
            }
            scanner.nextLine();
            passProbMap.fixScore(score);
        }
    }

}
