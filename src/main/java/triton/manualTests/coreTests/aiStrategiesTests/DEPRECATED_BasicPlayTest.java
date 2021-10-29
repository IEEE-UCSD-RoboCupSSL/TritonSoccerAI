package triton.manualTests.coreTests.aiStrategiesTests;

import triton.config.Config;
import triton.coreModules.ai.strategies.DEPRECATED_BasicPlay;
import triton.coreModules.ai.estimators.AttackSupportMapModule;
import triton.coreModules.ai.estimators.PassProbMapModule;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.RobotList;
import triton.manualTests.TritonTestable;

public class DEPRECATED_BasicPlayTest implements TritonTestable {
    private AttackSupportMapModule atkSupportMap;
    private PassProbMapModule passProbMap;
    DEPRECATED_BasicPlay basicPlay;
    private final RobotList<Ally> fielders;
    private final Ally keeper;
    private final RobotList<Foe> foes;
    private final Ball ball;
    public DEPRECATED_BasicPlayTest(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {
        this.fielders = fielders;
        this.keeper = keeper;
        this.foes = foes;
        this.ball = ball;
    }

    public boolean test(Config config) {
        atkSupportMap = new AttackSupportMapModule(fielders, foes, ball);
        passProbMap = new PassProbMapModule(fielders, foes, ball);
        basicPlay = new DEPRECATED_BasicPlay(fielders, keeper, foes, ball, atkSupportMap, passProbMap, config);
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
