package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Robot.Ally;
import Triton.Misc.Coordinates.Vec2D;

import java.util.Scanner;

public class KickTest extends RobotSkillsTest {
    Scanner scanner;
    Ally bot;

    public KickTest(Scanner scanner, Ally bot) {
        this.scanner = scanner;
        this.bot = bot;
    }

    @Override
    public boolean test() {
        try {
//            String line;
//            do {
//                System.out.println(">> ENTER \"start\" TO START:");
//                line = scanner.nextLine();
//            } while (!line.equals("start"));

            while (!bot.isHoldingBall()) {
                bot.getBall();
            }

            //Thread.sleep(300);
            bot.stop();

            double absAngleDiff;
            do {
                Vec2D center = new Vec2D(0, 0);
                Vec2D botToCenter = center.sub(bot.getData().getPos());
                double targetAngle = botToCenter.toPlayerAngle();
                bot.rotateTo(targetAngle);

                double botAngle = bot.getData().getAngle();
                absAngleDiff = Math.abs(targetAngle - botAngle);
            } while (absAngleDiff > 1);

            // Thread.sleep(100);
            bot.stop();

            System.out.println(">> ENTER FIRST SPEED AND SECOND SPEED TO KICK:");
            double kickSpeedHorizontal = scanner.nextDouble();
            double kickSpeedVertical = scanner.nextDouble();
            scanner.nextLine();
            bot.kick(new Vec2D(kickSpeedHorizontal, kickSpeedVertical));


            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }
}
