package Triton.ManualTests.PeriphMiscTests;

import Triton.App;

import Triton.Config.Config;
import Triton.ManualTests.MiscTests.ConstraintMappingMathTests;
import Triton.ManualTests.MiscTests.FutureTaskTest;
import Triton.ManualTests.MiscTests.PubSubTests;
import Triton.ManualTests.TritonTestable;

import java.util.TreeMap;

/**
 * --> Writing New Tests <--
 * Manual Tests are now automatically registered when you put the test name and an instantiation
 * of the test into `TestMaps`.
 *
 * PeriphMisc tests require no input therefore there is only a no-argument constructor for now.
 * If a new test requires some more inputs simply overload the constructor.
 *
 */
public class PeriphMiscTestFactory {

    private final TreeMap<String, TritonTestable> periphTestMap = new TreeMap<>();

    public PeriphMiscTestFactory(Config config) {
        periphTestMap.put("futask", new FutureTaskTest(App.threadPool));
        periphTestMap.put("pubsub", new PubSubTests(App.threadPool));
        periphTestMap.put("grsimvision", new GrSimVisionModuleTest_OldProto());
        periphTestMap.put("SSL", new SSLGameCtrlModuleTest());
        periphTestMap.put("vbotmath", new ConstraintMappingMathTests());
        periphTestMap.put("custom-P-queue-test", new CustomPriorityQueueTest());
    }

    public String[] getAvailableTestNames() {
        return periphTestMap.keySet().toArray(new String[0]);
    }

    public void printAvailableTestNames() {
        System.out.println("Available Tests:");
        for (String test : periphTestMap.keySet()) {
            System.out.printf("- %s \n", test);
        }
        System.out.println();
    }

    public TritonTestable getTest(String testName) {
        return periphTestMap.get(testName);
    }
}
