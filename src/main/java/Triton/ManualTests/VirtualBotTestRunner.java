package Triton.ManualTests;

import Triton.Config.Config;
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
}
