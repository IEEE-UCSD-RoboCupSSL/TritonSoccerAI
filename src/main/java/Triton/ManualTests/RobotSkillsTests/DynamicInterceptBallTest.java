package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;

import java.util.Scanner;

public class DynamicInterceptBallTest extends RobotSkillsTest {
    Scanner scanner;
    Ally ally;
    Ball ball;

    public DynamicInterceptBallTest(Scanner scanner, Ally ally, Ball ball) {
        this.scanner = scanner;
        this.ally = ally;
        this.ball = ball;
    }

    @Override
    public boolean test() {
        System.out.println("intercepting ball");
        while (true) {
            ally.dynamicIntercept(ball, 90);
        }
    }
}
