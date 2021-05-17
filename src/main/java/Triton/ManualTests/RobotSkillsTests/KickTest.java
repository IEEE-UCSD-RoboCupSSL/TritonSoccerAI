package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.Misc.Math.Matrix.Vec2D;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Scanner;

@AllArgsConstructor
public class KickTest extends RobotSkillsTest {
    Ally ally;
    Ball ball;


    @Override
    public boolean test() {
        Scanner scanner = new Scanner(System.in);
        try {
//            String line;
//            do {
//                System.out.println(">> ENTER \"start\" TO START:");
//                line = scanner.nextLine();
//            } while (!line.equals("start"));

            while (!ally.isHoldingBall()) {
                ally.getBall(ball);
            }

            //Thread.sleep(300);
            ally.stop();

            double absAngleDiff;
            do {
                Vec2D center = new Vec2D(0, 0);
                Vec2D botToCenter = center.sub(ally.getPos());
                double targetAngle = botToCenter.toPlayerAngle();
                ally.rotateTo(targetAngle);

                double botAngle = ally.getDir();
                absAngleDiff = Math.abs(targetAngle - botAngle);
            } while (absAngleDiff > 1);

            // Thread.sleep(100);
            ally.stop();

            System.out.println(">> ENTER FIRST SPEED AND SECOND SPEED TO KICK:");
            double kickSpeedHorizontal = scanner.nextDouble();
            double kickSpeedVertical = scanner.nextDouble();
            scanner.nextLine();
            ally.kick(new Vec2D(kickSpeedHorizontal, kickSpeedVertical));


            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }
}
