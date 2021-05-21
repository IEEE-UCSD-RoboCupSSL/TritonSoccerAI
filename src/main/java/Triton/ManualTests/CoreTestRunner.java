package Triton.ManualTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;

import java.util.Optional;
import java.util.Scanner;


/**
 * Test runner for core tests. When manual test mode is enabled. It outputs a list of currently registered tests
 * and prompts the user for input. It simply executes corresponding test given a test name.
 *
 * --> Writing New Tests <--
 * Refer to `CoreTestFactory` or `PeriphMiscTestFactory` for how to register new tests.
 */
public class CoreTestRunner {
    public static void runCoreTest(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {
        Scanner scanner = new Scanner(System.in);
        try {
            Thread.sleep(1000);
            CoreTestFactory testFactory = new CoreTestFactory(fielders, keeper, foes, ball);

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
}
