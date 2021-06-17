package Triton.ManualTests;

import Triton.Config.Config;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.SoccerObjects;

import java.util.Optional;
import java.util.Scanner;

public class VirtualBotTestRunner {
    public static void runVirtualBotTest(Config config, Scanner scanner, SoccerObjects soccerObjects) {
        try {
            VirtualBotTestFactory testFactory = new VirtualBotTestFactory(soccerObjects, config);
            String prevTestName = "";

            while (true) {
                boolean result = false;

                testFactory.printAvailableTestNames();
                System.out.println(">> ENTER TEST NAME:");
                String testName = scanner.nextLine();


                if (testName.equals("")) {
                    testName = prevTestName;
                } else if (testName.equals("quit")) {
                    break;
                }

                TritonTestable test = testFactory.getTest(testName);
                Optional<TritonTestable> test1 = Optional.ofNullable(test);

                if (test1.isEmpty()) {
                    System.out.println("Invalid Test Name");
                    continue;
                } else {
                    result = test1.get().test(config);
                }

                prevTestName = testName;
                System.out.println(result ? "Test Success" : "Test Fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("VirtualBot Ended");
    }

    public static void sendPrimitiveCommand(Scanner scanner, Ally bot) {
        System.out.println(">> Please Enter [Mode]: TDRD, TDRV, TVRD, TVRV");
        String mode = scanner.nextLine();
        switch (mode) {
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
    }

}
