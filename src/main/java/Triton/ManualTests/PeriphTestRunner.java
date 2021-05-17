package Triton.ManualTests;

import Triton.App;
import Triton.ManualTests.MiscTests.FutureTaskTest;
import Triton.ManualTests.MiscTests.PubSubTests;
import Triton.ManualTests.PeriphTests.OldGrsimVisionModuleTest;
import Triton.ManualTests.PeriphTests.SSLGameCtrlModuleTest;

import java.util.Scanner;

public class PeriphTestRunner {
    public static void runPeriphMiscTest(Scanner scanner) {
        boolean quit = false;
        String prevTestName = "";
        while (!quit) {
            System.out.println(">> ENTER TEST NAME:");
            String testName = scanner.nextLine();
            boolean rtn = false;
            int repeat = 0;
            do {
                switch (testName) {
                    case "sayhi" -> {
                        System.out.println("Hi!");
                        rtn = true;
                    }

                    case "futask" -> rtn = new FutureTaskTest(App.threadPool).test();
                    case "pubsub" -> rtn = new PubSubTests(App.threadPool, scanner).test();
                    case "grsimvision" -> rtn = new OldGrsimVisionModuleTest().test();
                    case "SSL" -> rtn = new SSLGameCtrlModuleTest().test();
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

        System.out.println("PeriphTest Ended");
        System.out.println("Automatically run CoreTest TestRunner next\n");
    }
}
