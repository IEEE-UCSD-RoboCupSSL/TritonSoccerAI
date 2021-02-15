package Triton.ManualTests.EstimatorTests;

import Triton.CoreModules.AI.Estimators.GapFinder;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.PeriphModules.Display.Display;
import Triton.PeriphModules.Display.PaintOption;

import java.util.ArrayList;

import static Triton.PeriphModules.Display.PaintOption.*;

public class GapFinderTest {

    GapFinder gapFinder;

    public GapFinderTest(RobotList<Ally> fielders, RobotList<Foe> foes, Ball ball) {
        gapFinder = new GapFinder(fielders, foes, ball);

    }

    public boolean test() {
        Display display = new Display();
        ArrayList<PaintOption> paintOptions = new ArrayList<>();
        paintOptions.add(GEOMETRY);
        paintOptions.add(OBJECTS);
        paintOptions.add(INFO);
        paintOptions.add(PROBABILITY);
        display.setPaintOptions(paintOptions);

        display.setProbFinder(gapFinder);

//        while(true) {
//
//            double[][] eval = gapFinder.getEval();
//            System.out.println(Arrays.deepToString(eval));
//
//            try {
//                Thread.sleep(1);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        return true;
    }
}
