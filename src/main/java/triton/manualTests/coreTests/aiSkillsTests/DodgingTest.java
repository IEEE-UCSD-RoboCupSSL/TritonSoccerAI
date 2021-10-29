package triton.manualTests.coreTests.aiSkillsTests;

import triton.config.Config;
import triton.coreModules.ai.skills.Dodging;
import triton.coreModules.ai.estimators.BasicEstimator;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.RobotList;
import triton.manualTests.coreTests.robotSkillsTests.RobotSkillsTest;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.Scanner;

public class DodgingTest extends RobotSkillsTest {
    Scanner scanner;
    RobotList<Ally> allies;
    RobotList<Foe> foes;
    Ball ball;
    Dodging dodging;

    public DodgingTest(RobotList<Ally> allies, Ally keeper, RobotList<Foe> foes, Ball ball) {
        //noinspection unchecked
        this.allies = (RobotList<Ally>) allies.clone();
        this.ball = ball;
        this.foes = foes;
        dodging = new Dodging(allies, foes, ball, new BasicEstimator(allies, keeper, foes, ball));
    }

    @Override
    public boolean test(Config config) {
        try {
            Ally holder = allies.get(2);

            while (!holder.isHoldingBall()) {
                holder.getBall(ball);
                Thread.sleep(1);
            }
            Vec2D holderPos = holder.getPos();
            holder.stop();
            Thread.sleep(1000);

            while(true) {
                if(!holder.isHoldingBall()) return false;
                dodging.dodge(holder, holderPos);
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }


}
