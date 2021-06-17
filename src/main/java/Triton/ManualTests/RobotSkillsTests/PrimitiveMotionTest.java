package Triton.ManualTests.RobotSkillsTests;

import Triton.Config.Config;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import lombok.AllArgsConstructor;

import java.util.Scanner;

@AllArgsConstructor
public class PrimitiveMotionTest extends RobotSkillsTest {
    Ally bot;

    @Override
    public boolean test(Config config) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Testing Primitive Motion Commands, Modes: TDRD, TDRV, TVRD, TVRV (T=Translational, R=Rotational, D=Displacement, V=Velocity)");
        boolean toQuit = false;
        while (!toQuit) {
            System.out.println(">> Please Enter [Mode] or quit to exit");
            String mode = scanner.nextLine();
            switch (mode) {
                case "quit" -> toQuit = true;
                case "TDRD", "TDRV", "TVRD", "TVRV" -> {
                    System.out.println(">> Please Enter [SetPoint.x] [SetPoint.y] [Angular SetPoint] ");
                    System.out.println("   Example Input: 0 0 0");
                    Vec2D setpoint = new Vec2D(0, 0);
                    setpoint.x = scanner.nextDouble();
                    setpoint.y = scanner.nextDouble();
                    double ang = scanner.nextDouble();
                    scanner.nextLine();

                    switch (mode) {
                        case "TDRD" -> {
                            bot.moveTo(setpoint);
                            bot.spinTo(ang);
                        }
                        case "TDRV" -> {
                            bot.moveTo(setpoint);
                            bot.spinAt(ang);
                        }
                        case "TVRD" -> {
                            bot.moveAt(setpoint);
                            bot.spinTo(ang);
                        }
                        case "TVRV" -> {
                            bot.moveAt(setpoint);
                            bot.spinAt(ang);
                        }
                    }
                }
                default -> {
                    bot.moveAt(new Vec2D(0, 0));
                    bot.spinAt(0);
                }
            }
            System.out.println("=====================================================");
        }
        return false;
    }
}
