package Triton.ManualTests.PeriphMiscTests;

import Triton.App;

import Triton.Config.Config;
import Triton.ManualTests.PeriphMiscTests.MiscTests.ConstraintMappingMathTests;
import Triton.ManualTests.PeriphMiscTests.MiscTests.FutureTaskTest;
import Triton.ManualTests.PeriphMiscTests.MiscTests.PubSubTests;
import Triton.ManualTests.PeriphMiscTests.PeriphTests.CustomPriorityQueueTest;
import Triton.ManualTests.PeriphMiscTests.PeriphTests.GrSimVisionModuleTest_OldProto;
import Triton.ManualTests.PeriphMiscTests.PeriphTests.SSLGameCtrlModuleTest;
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

    private final TreeMap<String, TritonTestable> periphMiscTestMap = new TreeMap<>();

    public PeriphMiscTestFactory(Config config) {
        periphMiscTestMap.put("futask", new FutureTaskTest(App.threadPool));
        periphMiscTestMap.put("pubsub", new PubSubTests(App.threadPool));
        periphMiscTestMap.put("grsimvision", new GrSimVisionModuleTest_OldProto());
        periphMiscTestMap.put("erforcevision", new ErForceSimVisionModuleTest());
        periphMiscTestMap.put("gc", new SSLGameCtrlModuleTest());
        periphMiscTestMap.put("vbotmath", new ConstraintMappingMathTests());
        periphMiscTestMap.put("custom-P-queue-test", new CustomPriorityQueueTest());
        periphMiscTestMap.put("detection", new DetectionTest_OldProto());

    }

    public String[] getAvailableTestNames() {
        return periphMiscTestMap.keySet().toArray(new String[0]);
    }

    public void printAvailableTestNames() {
        System.out.println("Available Tests:");
        for (String test : periphMiscTestMap.keySet()) {
            System.out.printf("- %s \n", test);
        }
        System.out.println();
    }

    public TritonTestable getTest(String testName) {
        return periphMiscTestMap.get(testName);
    }
}
