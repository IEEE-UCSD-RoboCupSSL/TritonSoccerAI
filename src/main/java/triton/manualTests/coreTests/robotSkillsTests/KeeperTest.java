package triton.manualTests.coreTests.robotSkillsTests;

import triton.config.Config;
import triton.coreModules.ai.estimators.BasicEstimator;
import triton.coreModules.ai.goalKeeping.GoalKeeping;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.RobotList;

public class KeeperTest extends RobotSkillsTest {
    RobotList<Ally> fielders;
    Ally keeper;
    RobotList<Foe> foes;
    Ball ball;

    BasicEstimator basicEstimator;
    GoalKeeping goalKeeping;

    public KeeperTest(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {
        this.fielders = fielders;
        this.keeper = keeper;
        this.foes = foes;
        this.ball = ball;

        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        goalKeeping = new GoalKeeping(keeper, ball, basicEstimator);
    }

    @Override
    public boolean test(Config config) {
        while (true) {
            goalKeeping.passiveGuarding();
        }
    }
}
