package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Robot.Ally.Ally;
import Triton.Misc.Math.Matrix.Vec2D;

import java.util.Scanner;


public class AdvancedMotionTest extends RobotSkillsTest {
    private Ally bot;

    @Override
    public boolean test() {
        Scanner scanner = new Scanner(System.in);
        try {
            boolean toQuit = false;
            while (!toQuit) {
                System.out.println(">>> Please Enter Method To Test: [strafe, curve, fast-curve, sprint, sprint-front] or quit");
                String mode = scanner.nextLine();
                System.out.println(">> Please Enter Target Position and Direction(optional): [x, y]  [degree](optional)");
                String inputTarget = scanner.nextLine();
                String[] target = inputTarget.split(" ");
                Vec2D targetPos = new Vec2D(0, 0);
                Double targetDir = null;
                if (target.length == 2) {
                    targetPos = new Vec2D(Double.parseDouble(target[0]), Double.parseDouble(target[1]));
                } else if (target.length == 3) {
                    targetPos = new Vec2D(Double.parseDouble(target[0]), Double.parseDouble(target[1]));
                    targetDir = Double.parseDouble(target[2]);
                } else {
                    System.out.println("Invalid Input");
                    continue;
                }

                switch (mode) {
                    case "quit" -> toQuit = true;
                    case "rotate" -> {
                        if (target == null) {
                            System.out.println("Invalid Input");
                        }
                        while (!bot.isDirAimed(targetDir)) {
                            bot.rotateTo(targetDir);
                            Thread.sleep(1);
                        }
                    }
                    case "strafe" -> {
                        if (targetDir == null) {
                            while (!bot.isPosArrived(targetPos)) {
                                bot.strafeTo(targetPos);
                                Thread.sleep(1);
                            }
                        } else {
                            while (!bot.isPosArrived(targetPos) && !bot.isDirAimed(targetDir)) {
                                bot.strafeTo(targetPos, targetDir);
                                Thread.sleep(1);
                            }
                        }
                    }
                    case "curve" -> {
                        if (targetDir == null) {
                            while (!bot.isPosArrived(targetPos)) {
                                bot.curveTo(targetPos);
                                Thread.sleep(1);
                            }
                        } else {
                            while (!bot.isPosArrived(targetPos) && !bot.isDirAimed(targetDir)) {
                                bot.curveTo(targetPos, targetDir);
                                Thread.sleep(1);
                            }
                        }
                    }
                    case "fast-curve" -> {
                        if (targetDir == null) {
                            while (!bot.isPosArrived(targetPos)) {
                                bot.fastCurveTo(targetPos);
                                Thread.sleep(1);
                            }
                        } else {
                            while (!bot.isPosArrived(targetPos) && !bot.isDirAimed(targetDir)) {
                                bot.fastCurveTo(targetPos, targetDir);
                                Thread.sleep(1);
                            }
                        }
                    }

                    case "sprint" -> {
                        if (targetDir == null) {
                            while (!bot.isPosArrived(targetPos)) {
                                bot.sprintTo(targetPos);
                                Thread.sleep(1);
                            }
                        } else {
                            while (!bot.isPosArrived(targetPos) && !bot.isDirAimed(targetDir)) {
                                bot.sprintTo(targetPos, targetDir);
                                Thread.sleep(1);
                            }
                        }
                    }
                    case "sprint-front" -> {
                        if (targetDir == null) {
                            while (!bot.isPosArrived(targetPos)) {
                                bot.sprintFrontTo(targetPos);
                                Thread.sleep(1);
                            }
                        } else {
                            while (!bot.isPosArrived(targetPos) && !bot.isDirAimed(targetDir)) {
                                bot.sprintFrontTo(targetPos, targetDir);
                                Thread.sleep(1);
                            }
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
