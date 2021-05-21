package Triton.ManualTests;

import Triton.App;

import Triton.ManualTests.MiscTests.FutureTaskTest;
import Triton.ManualTests.MiscTests.PubSubTests;
import Triton.ManualTests.PeriphTests.OldGrsimVisionModuleTest;
import Triton.ManualTests.PeriphTests.SSLGameCtrlModuleTest;

import java.util.TreeMap;

/**
 * --> Writing New Tests <--
 * Manual Tests are now automatically registered when you put the test name and an instantiation
 * of the test into `TestMaps`.
 *
 * PeriphMisc tests require no input therefore there is only a no-argument constructor for now.
 * If a new test requires some more inputs simply overloadthe constructor.
 *
 */
public class PeriphMiscTestFactory {

    private final TreeMap<String, TritonTestable> periphTestMap = new TreeMap<>();

    public PeriphMiscTestFactory() {
        periphTestMap.put("futask", new FutureTaskTest(App.threadPool));
        periphTestMap.put("pubsub", new PubSubTests(App.threadPool));
        periphTestMap.put("grsimvision", new OldGrsimVisionModuleTest());
        periphTestMap.put("SSL", new SSLGameCtrlModuleTest());
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
