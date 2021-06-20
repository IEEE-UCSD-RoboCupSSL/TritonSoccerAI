package Triton.ManualTests.CoreTests.EstimatorTests;

import Triton.Config.Config;
import Triton.CoreModules.AI.Estimators.PassProbMapModule;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.TritonTestable;
import Triton.PeriphModules.Display.Display;
import Triton.PeriphModules.Display.PaintOption;

import java.util.ArrayList;
import java.util.Scanner;

import static Triton.PeriphModules.Display.PaintOption.*;
import static Triton.PeriphModules.Display.PaintOption.PROBABILITY;

public class PassProbMapTest implements TritonTestable {

    PassProbMapModule passProbMap;

    public PassProbMapTest(RobotList<Ally> fielders, RobotList<Foe> foes, Ball ball) {
        passProbMap = new PassProbMapModule(fielders, foes, ball);
        passProbMap.run();
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
