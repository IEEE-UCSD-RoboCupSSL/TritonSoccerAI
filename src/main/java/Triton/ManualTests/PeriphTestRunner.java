package Triton.ManualTests;

import Triton.App;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.MiscTests.FutureTaskTest;
import Triton.ManualTests.MiscTests.PubSubTests;
import Triton.ManualTests.PeriphTests.OldGrsimVisionModuleTest;
import Triton.ManualTests.PeriphTests.SSLGameCtrlModuleTest;

import java.util.Optional;
import java.util.Scanner;

public class PeriphTestRunner {
    public static void runPeriphMiscTest() {
        Scanner scanner = new Scanner(System.in);
        try {
            PeriphMiscTestFactory testFactory = new PeriphMiscTestFactory();
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

        System.out.println("PeriphTest Ended");
        System.out.println("Automatically run CoreTest TestRunner next\n");
    }
}
