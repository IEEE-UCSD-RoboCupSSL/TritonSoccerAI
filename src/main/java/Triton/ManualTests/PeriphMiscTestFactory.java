package Triton.ManualTests;

import Triton.App;

import Triton.ManualTests.MiscTests.FutureTaskTest;
import Triton.ManualTests.MiscTests.PubSubTests;
import Triton.ManualTests.PeriphTests.OldGrsimVisionModuleTest;
import Triton.ManualTests.PeriphTests.SSLGameCtrlModuleTest;

import java.util.TreeMap;

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
        int counter = 0;
        System.out.println("Available Tests:");
        for (String test : periphTestMap.keySet()) {
            System.out.printf("%d. %s \n", counter++, test);
        }
        System.out.println("");
    }

    public TritonTestable getTest(String testName) {
        return periphTestMap.get(testName);
    }

    public TritonTestable getTest(int testIndex){
        String testName = (String) periphTestMap.keySet().toArray()[testIndex];
        return periphTestMap.get(testName);
    }


}
