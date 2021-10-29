package triton.manualTests.coreTests.aiTacticsTests;

import triton.config.Config;
import triton.coreModules.ai.tactics.FillGapGetBall;
import triton.coreModules.ai.estimators.BasicEstimator;
import triton.coreModules.ai.estimators.AttackSupportMapModule;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.Robot;
import triton.coreModules.robot.RobotList;
import triton.manualTests.TritonTestable;
import triton.periphModules.display.Display;
import triton.periphModules.display.PaintOption;

import java.util.ArrayList;

import static triton.periphModules.display.PaintOption.*;

public class GapGetBallTest implements TritonTestable {

    private BasicEstimator basicEstimator;
    private FillGapGetBall fillGapGetBall;

    private final RobotList<Ally> fielders;
    private final Ally keeper;
    private final RobotList<Foe> foes;
    private final Ball ball;

    public GapGetBallTest(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {
        this.fielders = fielders;
        this.keeper = keeper;
        this.foes = foes;
        this.ball = ball;

    }

    public boolean test(Config config) {
        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        fillGapGetBall = new FillGapGetBall(fielders, keeper, foes, ball,
                new AttackSupportMapModule(fielders, foes, ball), config);


        Display display = new Display(config);
        ArrayList<PaintOption> paintOptions = new ArrayList<>();
        paintOptions.add(GEOMETRY);
        paintOptions.add(OBJECTS);
        paintOptions.add(INFO);
        paintOptions.add(PROBABILITY);
        display.setPaintOptions(paintOptions);

        display.setProbFinder(fillGapGetBall.getatkSupportMap());
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
