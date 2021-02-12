package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;

import java.util.Scanner;

public class InterceptBallTest extends RobotSkillsTest {
    Scanner scanner;
    Ally ally;
    Ball ball;

    public InterceptBallTest(Scanner scanner, Ally ally, Ball ball) {
        this.scanner = scanner;
        this.ally = ally;
        this.ball = ball;
    }

    @Override
    public boolean test() {
        System.out.println("intercepting ball");
        while (!ally.isHoldingBall()) {
//            double faceDir;
//            if (ball.getVel().mag() < 10) {
//                faceDir = ball.getPos().sub(ally.getPos()).toPlayerAngle();
//            } else {
//                faceDir = ball.getVel().scale(-1).toPlayerAngle();
//            }

            ally.dynamicIntercept(ball, 90);
        }

        ally.stop();
        return true;
    }
}
