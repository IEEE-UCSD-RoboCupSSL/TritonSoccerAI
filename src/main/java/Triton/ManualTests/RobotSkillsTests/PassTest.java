package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.Misc.Coordinates.Vec2D;

import java.util.Scanner;

public class PassTest extends RobotSkillsTest {
    Scanner scanner;
    Ally ally;
    Ball ball;

    public PassTest(Scanner scanner, Ally ally, Ball ball) {
        this.scanner = scanner;
        this.ally = ally;
        this.ball = ball;
    }

    @Override
    public boolean test() {
        try {
            Vec2D target = new Vec2D(1000, 1000);

            while (!ally.isHoldingBall()) {
                ally.getBall(ball);
            }

            while (!ally.isPosArrived(new Vec2D(0, 0)) || !ally.isDirAimed(0)) {
                ally.fastCurveTo(new Vec2D(0, 0), 0);
            }

            while (ally.isHoldingBall()) {
                ally.passBall(target, 0.5);
            }

            long t0 = System.currentTimeMillis();
            double dist;
            do {
                dist = target.sub(ball.getPos()).mag();
            } while (dist > 200);

            System.out.println(System.currentTimeMillis() - t0);

            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }
}
