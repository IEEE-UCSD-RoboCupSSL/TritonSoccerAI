package Triton.ManualTests.AI_TacticsTests;

import Triton.CoreModules.AI.AI_Tactics.FillGapGetBall;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.GapFinder;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;
import Triton.PeriphModules.Display.Display;
import Triton.PeriphModules.Display.PaintOption;
import org.ejml.All;

import java.util.ArrayList;

import static Triton.PeriphModules.Display.PaintOption.*;

public class GapGetBallTest {

    private final BasicEstimator basicEstimator;
    private final FillGapGetBall fillGapGetBall;


    public GapGetBallTest(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {
        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        fillGapGetBall = new FillGapGetBall(fielders, keeper, foes, ball);
    }

    public boolean test() {
        Display display = new Display();
        ArrayList<PaintOption> paintOptions = new ArrayList<>();
        paintOptions.add(GEOMETRY);
        paintOptions.add(OBJECTS);
        paintOptions.add(INFO);
        paintOptions.add(PROBABILITY);
        display.setPaintOptions(paintOptions);

        display.setGapFinder(fillGapGetBall.getGapFinder());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while(true) {
           boolean rtn = fillGapGetBall.exec();
           System.out.println(rtn);
           Robot holder = basicEstimator.getBallHolder();
           if(holder != null) {
               ((Ally) holder).stop();
           }

           try {
                Thread.sleep(1);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
        }


        // return true;
    }
}
