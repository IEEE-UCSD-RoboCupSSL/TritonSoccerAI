package Triton.ManualTests;

import Triton.App;
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
import Triton.ManualTests.MiscTests.FutureTaskTest;
import Triton.ManualTests.MiscTests.PubSubTests;
import Triton.ManualTests.PeriphTests.OldGrsimVisionModuleTest;
import Triton.ManualTests.PeriphTests.SSLGameCtrlModuleTest;
import Triton.ManualTests.RobotSkillsTests.*;
import Triton.ManualTests.RobotSkillsTests.AsyncSkillsTests.SimpleProceduralSkillDemo;

import java.util.HashMap;

public class PeriphMiscTestFactory {

    private final HashMap<String, TritonTestable> periphTestMap = new HashMap<>();

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


}
