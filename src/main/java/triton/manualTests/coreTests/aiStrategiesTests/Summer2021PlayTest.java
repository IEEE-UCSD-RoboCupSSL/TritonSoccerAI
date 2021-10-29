package triton.manualTests.coreTests.aiStrategiesTests;

import triton.config.Config;
import triton.coreModules.ai.strategies.Summer2021Play;
import triton.coreModules.ai.estimators.AttackSupportMapModule;
import triton.coreModules.ai.estimators.PassProbMapModule;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.RobotList;
import triton.manualTests.TritonTestable;

public class Summer2021PlayTest implements TritonTestable  {
    private AttackSupportMapModule atkSupportMap;
    private PassProbMapModule passProbMap;
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
        atkSupportMap = new AttackSupportMapModule(fielders, foes, ball);
        passProbMap = new PassProbMapModule(fielders, foes, ball);
        play = new Summer2021Play(fielders, keeper, foes, ball, atkSupportMap, passProbMap, config);
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
