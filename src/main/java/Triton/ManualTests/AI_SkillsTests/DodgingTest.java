package Triton.ManualTests.AI_SkillsTests;

import Triton.CoreModules.AI.AI_Skills.Dodging;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.RobotSkillsTests.RobotSkillsTest;
import Triton.Misc.Math.Matrix.Vec2D;

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
    public boolean test() {
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
