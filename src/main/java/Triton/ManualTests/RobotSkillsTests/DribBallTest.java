package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.Misc.Math.Matrix.Vec2D;
import lombok.AllArgsConstructor;

import java.util.Scanner;

import static Triton.Config.OldConfigs.PathfinderConfig.DRIB_ROTATE_DIST;

@AllArgsConstructor
public class DribBallTest extends RobotSkillsTest {
    Ally ally;
    Ball ball;


    @Override
    public boolean test() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("getting ball");

        while (!ally.isHoldingBall()) {
            ally.getBall(ball);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

                Vec2D angleUnitDir = new Vec2D(targetAngle);
                Vec2D angleOffsetVec = angleUnitDir.scale(DRIB_ROTATE_DIST);
                Vec2D targetPos = ball.getPos().sub(angleOffsetVec);

                boolean held = true;

                while ((!ally.isDirAimed(targetAngle) || !ally.isPosArrived(targetPos, 30)) && held) {
                    held = ally.dribRotate(ball, targetAngle);

                    angleUnitDir = new Vec2D(targetAngle);
                    angleOffsetVec = angleUnitDir.scale(DRIB_ROTATE_DIST);
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
