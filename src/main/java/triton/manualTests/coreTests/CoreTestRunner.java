package triton.manualTests.coreTests;

import triton.config.Config;
import triton.manualTests.testUtil.TestUtil;
import triton.manualTests.TritonTestable;
import triton.SoccerObjects;

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
    public static void runCoreTest(Config config, SoccerObjects soccerObjects, Scanner scanner) {
        try {
            CoreTestFactory testFactory = new CoreTestFactory(soccerObjects, config);
            Thread.sleep(1000);
            TritonTestable defaultFormation = testFactory.getTest("defaultFormation");
            Optional<TritonTestable> defaultFormation1 = Optional.of(defaultFormation);
            defaultFormation1.get().test(config);
            //defaultFormation.test(config);

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
                if(test == null) {
                    System.out.println("Invalid Test Name");
                    TestUtil.enterKeyToContinue();
                    continue;
                } else {
                    result = test.test(config);
                }

                prevTestName = testName;
                System.out.printf("[%s] ", testName);
                System.out.println(result ? "Test Success" : "Test Fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("CoreTest TestRunner Ended");
    }
}
