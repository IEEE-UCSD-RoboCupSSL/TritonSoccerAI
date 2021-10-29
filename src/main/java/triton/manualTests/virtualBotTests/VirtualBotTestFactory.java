package triton.manualTests.virtualBotTests;

import triton.config.Config;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.RobotList;
import triton.manualTests.TritonTestable;
import triton.SoccerObjects;

import java.util.TreeMap;

public class VirtualBotTestFactory {
    private final TreeMap<String, TritonTestable> virtualBotTestMap = new TreeMap<>();
    
    public VirtualBotTestFactory(SoccerObjects soccerObjects, Config config) {
        RobotList<Ally> fielders = soccerObjects.fielders;
        Ally keeper = soccerObjects.keeper;
        RobotList<Foe> foes = soccerObjects.foes;
        Ball ball = soccerObjects.ball;
        virtualBotTestMap.put("vmcutop", new VirtualMcuTopModuleTest(soccerObjects));
        virtualBotTestMap.put("grsim-client", new SimClientModuleTest());
        virtualBotTestMap.put("vbot", new VirtualBotModuleTest(soccerObjects));
        virtualBotTestMap.put("tdrd", new TdrdTest(soccerObjects));
    }

    public String[] getAvailableTestNames() {
        return virtualBotTestMap.keySet().toArray(new String[0]);
    }

    public void printAvailableTestNames() {
        System.out.println("Available Tests:");
        for (String test : virtualBotTestMap.keySet()) {
            System.out.printf("- %s \n", test);
        }
        System.out.println();
    }

    public TritonTestable getTest(String testName) {
        return virtualBotTestMap.get(testName);
    }
}
