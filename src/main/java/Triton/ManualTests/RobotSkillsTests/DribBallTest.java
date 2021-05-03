package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.Misc.Math.Matrix.Vec2D;

import java.util.Scanner;

import static Triton.Config.ObjectConfig.*;

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

                while (!ally.isHoldingBall()) {
                    ally.getBall(ball);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Vec2D angleUnitVec = new Vec2D(targetAngle);
                Vec2D angleOffsetVec = angleUnitVec.scale(ROBOT_RADIUS + BALL_RADIUS);
                Vec2D targetPos = ball.getPos().sub(angleOffsetVec);
                boolean held = true;

                while ((!ally.isDirAimed(targetAngle) || !ally.isPosArrived(targetPos, 20)) && held) {
                    held = ally.dribRotate(ball, targetAngle);

                    angleUnitVec = new Vec2D(targetAngle);
                    angleOffsetVec = angleUnitVec.scale(ROBOT_RADIUS + BALL_RADIUS);
                    targetPos = ball.getPos().sub(angleOffsetVec);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                ally.stop();
            }
        }
        return true;
    }
}
