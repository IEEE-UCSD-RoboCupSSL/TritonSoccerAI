package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.GoalKeeping.GoalKeeping;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;

import java.util.Scanner;

public class KeeperTest extends RobotSkillsTest {
    Scanner scanner;
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
    public boolean test() {
        while (true) {
            goalKeeping.passiveGuarding();
        }
    }
}
