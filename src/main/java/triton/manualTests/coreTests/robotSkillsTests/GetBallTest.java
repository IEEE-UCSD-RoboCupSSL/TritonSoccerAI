package triton.manualTests.coreTests.robotSkillsTests;

import triton.config.Config;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;

import static triton.Util.delay;


public class GetBallTest extends RobotSkillsTest {
    Ally ally;
    Ball ball;

    public GetBallTest(Ally ally, Ball ball) {
        this.ally = ally;
        this.ball = ball;
    }

    @Override
    public boolean test(Config config) {
//        String line;
//        do {
//            System.out.println(">> ENTER \"start\" TO START:");
//            line = scanner.nextLine();
//        } while (!line.equals("start"));

        while (!ally.isHoldingBall()) {
            ally.getBall(ball);
            System.out.println(ball.getPos());
            delay(3);
        }

        ally.stop();
        long t0 = System.currentTimeMillis();
        while(System.currentTimeMillis() - t0 < 1000) {
            System.out.println(ball.getPos());
        }

        return true;
    }
}
