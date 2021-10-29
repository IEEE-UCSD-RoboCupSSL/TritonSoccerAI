package triton.manualTests.coreTests.robotSkillsTests;

import triton.config.Config;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.misc.math.linearAlgebra.Vec2D;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HoldBallPosTest extends RobotSkillsTest {
    Ally ally;
    Ball ball;


    @Override
    public boolean test(Config config) {
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
