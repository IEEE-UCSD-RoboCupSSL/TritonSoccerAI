package Triton.ManualTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.AI_SkillsTests.CPassTest;
import Triton.ManualTests.AI_SkillsTests.DodgingTest;
import Triton.ManualTests.AI_SkillsTests.GroupToTest;
import Triton.ManualTests.AI_SkillsTests.ShootGoalTest;
import Triton.ManualTests.AI_StrategiesTests.BasicPlayTest;
import Triton.ManualTests.AI_TacticsTests.DefendPlanATest;
import Triton.ManualTests.AI_TacticsTests.GapGetBallTest;
import Triton.ManualTests.EstimatorTests.GapFinderTest;
import Triton.ManualTests.EstimatorTests.PassFinderTest;
import Triton.ManualTests.RobotSkillsTests.*;
import Triton.ManualTests.RobotSkillsTests.AsyncSkillsTests.SimpleProceduralSkillDemo;

import java.util.Scanner;

public class CoreTestRunner {
    public static void runCoreTest(Scanner scanner, RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {
        try {
            Thread.sleep(1000);
            new FormationTest("tester", fielders).test();

            String prevTestName = "";
            boolean quit = false;
            while (!quit) {
                System.out.println(">> ENTER TEST NAME:");
                String testName = scanner.nextLine();
                boolean rtn = false;
                int repeat = 0;
                do {
                    switch (testName) {
                        case "pmotion" -> rtn = new PrimitiveMotionTest(scanner, fielders.get(3)).test();
                        case "amotion" -> rtn = new AdvancedMotionTest(scanner, fielders.get(1)).test();
                        case "getball" -> rtn = new GetBallTest(scanner, fielders.get(3), ball).test();
                        case "kick" -> rtn = new KickTest(scanner, fielders.get(3), ball).test();
                        case "misc" -> rtn = new MiscTest(scanner, fielders.get(3), ball).test();
                        case "cpass" -> rtn = new CPassTest(scanner, fielders, keeper, foes, ball).test();
                        case "group" -> rtn = new GroupToTest(scanner, fielders, ball).test();
                        case "drib" -> rtn = new DribBallTest(scanner, fielders.get(1), ball).test();
                        case "vel" -> rtn = new VelTest(scanner, fielders.get(0)).test();
                        case "inter" -> rtn = new DynamicInterceptBallTest(scanner, fielders.get(1), ball).test();
                        case "collect" -> rtn = new DataCollector(scanner, fielders, keeper, ball).test();
                        case "reset" -> rtn = new FormationTest("tester", fielders).test();
                        case "formation" -> rtn = new FormationTest(scanner, fielders, keeper).test();
                        case "gap" -> rtn = new GapFinderTest(fielders, foes, ball).test();
                        case "pass" -> rtn = new PassFinderTest(scanner, fielders, foes, ball).test();
                        case "gapgetball" -> rtn = new GapGetBallTest(fielders, keeper, foes, ball).test();
                        case "shoot" -> rtn = new ShootGoalTest(scanner, fielders.get(0), foes, ball).test();
                        case "keep" -> rtn = new KeeperTest(fielders, keeper, foes, ball).test();
                        case "defendA" -> rtn = new DefendPlanATest(fielders, keeper, foes, ball).test();
                        case "dodge" -> rtn = new DodgingTest(fielders, keeper, foes, ball).test();
                        case "holdballpos" -> rtn = new HoldBallPosTest(fielders.get(3), ball).test();
                        case "basicplay" -> rtn = new BasicPlayTest(fielders, keeper, foes, ball).test();
                        case "capDetect" -> rtn = new CapDetectionTest(scanner, fielders.get(3), ball).test();
                        case "spsdemo" -> rtn = new SimpleProceduralSkillDemo(scanner, fielders, ball).test();
                        case "quit" -> {
                            quit = true;
                            rtn = true;
                        }
                        case "" -> {
                            repeat++;
                            testName = prevTestName;
                        }
                        default -> System.out.println("Invalid Test Name");
                    }
                } while (repeat-- > 0);
                repeat = 0;
                prevTestName = testName;
                if(!quit) System.out.println(rtn ? "Test Success" : "Test Fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("CoreTest TestRunner Ended, Enter Ctrl+C to Exit");
    }
}
