package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.Misc.Math.Matrix.Vec2D;

import java.util.Scanner;

public class HoldBallPosTest extends RobotSkillsTest {
    Ally ally;
    Ball ball;

    public HoldBallPosTest(Ally ally, Ball ball) {

        this.ally = ally;
        this.ball = ball;
    }

    @Override
    public boolean test() {
        try {
            while(true) {
                if(ally.isHoldingBall()) {
                    ally.curveTo(new Vec2D(0, 0));
                } else  {
                    ally.getBall(ball);
                }

                System.out.println(ally.HoldBallPos());

                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ally.stop();
        return true;
    }
}
