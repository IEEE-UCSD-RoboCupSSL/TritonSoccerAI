package Triton.ManualTests.AI_StrategiesTests;

import Triton.CoreModules.AI.AI_Strategies.BasicPlay;
import Triton.CoreModules.AI.Estimators.GapFinder;
import Triton.CoreModules.AI.Estimators.PassFinder;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.TritonTestable;

public class BasicPlayTest implements TritonTestable {
    private final GapFinder gapFinder;
    private final PassFinder passFinder;
    BasicPlay basicPlay;
    public BasicPlayTest(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {

        gapFinder = new GapFinder(fielders, foes, ball);
        passFinder = new PassFinder(fielders, foes, ball);
        basicPlay = new BasicPlay(fielders, keeper, foes, ball, gapFinder, passFinder);
    }

    public boolean test() {
        try {
            while (true) {
                basicPlay.play();
                Thread.sleep(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
