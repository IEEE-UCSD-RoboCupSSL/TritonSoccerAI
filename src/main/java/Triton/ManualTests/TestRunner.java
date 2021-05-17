package Triton.ManualTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
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
import Triton.Misc.Math.Matrix.Mat2D;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.Misc.ModulePubSubSystem.Module;

import java.util.Optional;
import java.util.Scanner;

public class TestRunner implements Module {

    private final RobotList<Ally> fielders;
    private final Ally keeper;
    private final RobotList<Foe> foes;
    private final Ball ball;

    public TestRunner(RobotList<Ally> fielders,
                      Ally keeper, RobotList<Foe> foes, Ball ball) {
        this.fielders = fielders;
        this.keeper = keeper;
        this.foes = foes;
        this.ball = ball;

    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        try {
            Thread.sleep(1000);
            TestFactory testFactory = new TestFactory(fielders, keeper, foes, ball);

            TritonTestable defaultFormation = testFactory.getTest("defaultFormation");
            Optional<TritonTestable> defaultFormation1 = Optional.of(defaultFormation);
            defaultFormation1.get().test();

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
                    result = test1.get().test();
                }

                prevTestName = testName;
                System.out.println(result ? "Test Success" : "Test Fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println("CoreTest TestRunner Ended, Enter Ctrl+C to Exit");
    }


//    private void miscMathTests() {
//        System.out.println(Mat2D.rotation(90).mult(new Vec2D(1, 0)));
//        System.out.println(Mat2D.rotation(90).mult(new Vec2D(0, 1)));
//        System.out.println(Mat2D.rotation(90).mult(new Vec2D(-1, 0)));
//        System.out.println(Mat2D.rotation(90).mult(new Vec2D(0, -1)));
//
//        System.out.println(Mat2D.rotation(90).mult(new Vec2D(10, 10)));
//    }


}
