package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.Misc.Coordinates.Vec2D;

import java.util.Scanner;

public class DribBallTest extends RobotSkillsTest {
    Scanner scanner;
    Ally ally;
    Ball ball;

    public DribBallTest(Scanner scanner, Ally ally, Ball ball) {
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

        ally.stop();

        while (true) {
            System.out.println(">> ENTER TARGET ANGLE:");
            String line = scanner.nextLine();

            if (line.equals("quit"))
                break;
            else {
                double targetAngle = Double.parseDouble(line);
                while (!ally.isDirAimed(targetAngle)) {
                    ally.dribRotate(ball, targetAngle);
                }
            }
        }
        return true;
    }
}
