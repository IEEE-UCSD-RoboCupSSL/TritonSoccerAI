package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.Misc.Coordinates.Vec2D;

import java.util.Scanner;

public class MiscTest extends RobotSkillsTest {
    Scanner scanner;
    Ally ally;
    Ball ball;

    public MiscTest(Scanner scanner, Ally ally, Ball ball) {
        this.scanner = scanner;
        this.ally = ally;
        this.ball = ball;
    }

    @Override
    public boolean test() {
        System.out.println("getting ball");
        while (!ally.isHoldingBall()) {
            ally.getBall(ball);
        }

        System.out.println("moving to 0, 0");
        while (!ally.isPosArrived(new Vec2D(0, 0))) {
            ally.sprintTo(new Vec2D(0, 0));
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

        System.out.println("moving 1 meter");
        while (!ally.isMaxDispExceeded()) {
            double disp = ally.dispSinceHoldBall();
            System.out.println(disp);
            ally.sprintTo(new Vec2D(2000, 2000));
        }

        ally.stop();

        return true;
    }
}
