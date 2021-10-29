package triton.manualTests.coreTests.robotSkillsTests;

import triton.config.Config;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class FlankBallTest extends RobotSkillsTest {
    Ally ally;
    Ball ball;


    @Override
    public boolean test(Config config) {
        System.out.println("intercepting ball");
        ally.stop();
        while (true) {
            if(ally.isHoldingBall()) {
                ally.stop();
            } else {
                ally.dynamicIntercept(ball, 90.0);
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
