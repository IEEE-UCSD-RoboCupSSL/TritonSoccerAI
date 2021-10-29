package triton.manualTests.coreTests.robotSkillsTests;

import triton.config.Config;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.misc.math.linearAlgebra.Vec2D;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MiscTest extends RobotSkillsTest {
    Ally ally;
    Ball ball;


    @Override
    public boolean test(Config config) {
        System.out.println("getting ball");
        while (!ally.isHoldingBall()) {
            ally.getBall(ball);
        }

        System.out.println("moving to 0, 0");
        while (!ally.isPosArrived(new Vec2D(0, 0))) {
            ally.curveTo(new Vec2D(0, 0));
        }

        System.out.println("rotating to 0");
        while (!ally.isDirAimed(0)) {
            ally.rotateTo(0);
        }

        System.out.println("rotating to 180");
        while (!ally.isDirAimed(180)) {
            ally.rotateTo(180);
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("kicking");
        ally.kick(new Vec2D(2, 0));

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("getting ball");
        while (!ally.isHoldingBall()) {
            ally.getBall(ball);
        }


        ally.stop();

        return true;
    }
}
