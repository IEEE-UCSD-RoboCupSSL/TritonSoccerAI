package Triton.ManualTests.CoreTests.AI_StrategiesTests;

import Triton.Config.Config;
import Triton.CoreModules.AI.AI_Strategies.Summer2021Play;
import Triton.CoreModules.AI.Estimators.GapFinder;
import Triton.CoreModules.AI.Estimators.PassFinder;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.TritonTestable;

public class Summer2021PlayTest implements TritonTestable  {
    private GapFinder gapFinder;
    private PassFinder passFinder;
    Summer2021Play play;
    private final RobotList<Ally> fielders;
    private final Ally keeper;
    private final RobotList<Foe> foes;
    private final Ball ball;
    public Summer2021PlayTest(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {
        this.fielders = fielders;
        this.keeper = keeper;
        this.foes = foes;
        this.ball = ball;
    }

    public boolean test(Config config) {
        gapFinder = new GapFinder(fielders, foes, ball);
        passFinder = new PassFinder(fielders, foes, ball);
        play = new Summer2021Play(fielders, keeper, foes, ball, gapFinder, passFinder, config);
        try {
            while (true) {
                play.play();
                Thread.sleep(3);
                System.out.println("CurrState: " + play.getCurrState());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

}
