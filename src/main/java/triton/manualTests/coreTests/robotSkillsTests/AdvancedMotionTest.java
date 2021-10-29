package triton.manualTests.coreTests.robotSkillsTests;

import triton.config.Config;
import triton.coreModules.robot.ally.Ally;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.Scanner;

import static triton.Util.delay;

public class AdvancedMotionTest extends RobotSkillsTest {
    private final Ally bot;

    public AdvancedMotionTest(Ally bot) {
        this.bot = bot;
    }

    @Override
    public boolean test(Config config) {
        Scanner scanner = new Scanner(System.in);
        try {
            boolean toQuit = false;
            while (!toQuit) {
                System.out.println(">>> Please Enter Method To Test: [rotate, strafe, curve, fast-curve, sprint, sprint-front] or quit");
                String mode = scanner.nextLine();
                System.out.println(">> Please Enter Target Position and Direction(optional): [x, y]  [degree] (degree could be a dummy value if the test doesn't need it)");
                Vec2D targetPos = new Vec2D(scanner.nextDouble(), scanner.nextDouble());
                double targetDir = scanner.nextDouble();
                scanner.nextLine();
                System.out.println("Entered numbers: " + targetPos + ", " + targetDir);

                switch (mode) {
                    case "quit" -> toQuit = true;
                    case "rotate" -> {
                        while (!bot.isDirAimed(targetDir)) {
                            bot.rotateTo(targetDir);
                            Thread.sleep(1);
                        }
                    }
                    case "strafe" -> {
                        System.out.println("TargetPos: " + targetPos + " BotPos: " + bot.getPos());
                        while (!bot.isPosArrived(targetPos) || !bot.isDirAimed(targetDir)) {

                            System.out.println("TargetPos: " + targetPos + " BotPos: " + bot.getPos());
                            bot.strafeTo(targetPos, targetDir);
                            delay(1);
                        }
                    }
                    case "curve" -> {
                        while (!bot.isPosArrived(targetPos) || !bot.isDirAimed(targetDir)) {
                            bot.curveTo(targetPos, targetDir);
                            delay(1);
                        }
                    }
                    case "fast-curve" -> {
                        while (!bot.isPosArrived(targetPos) || !bot.isDirAimed(targetDir)) {
                            bot.fastCurveTo(targetPos, targetDir);
                            delay(1);
                        }
                    }

                    case "sprint" -> {
                        while (!bot.isPosArrived(targetPos) || !bot.isDirAimed(targetDir)) {
                            bot.sprintTo(targetPos, targetDir);
                            delay(1);
                        }
                    }
                    case "sprint-front" -> {
                        while (!bot.isPosArrived(targetPos) || !bot.isDirAimed(targetDir)) {
                            bot.sprintFrontTo(targetPos, targetDir);
                            delay(1);
                        }
                    }
                    default -> {
                        System.out.println("Invalid Method Name");
                    }
                }
                System.out.println("=====================================================");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}
