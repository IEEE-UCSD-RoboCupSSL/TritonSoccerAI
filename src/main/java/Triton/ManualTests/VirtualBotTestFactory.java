package Triton.ManualTests;

import Triton.Config.Config;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.VirtualBotTests.GrSimClientModuleTest;
import Triton.ManualTests.VirtualBotTests.TdrdTest;
import Triton.ManualTests.VirtualBotTests.VirtualBotModuleTest;
import Triton.ManualTests.VirtualBotTests.VirtualMcuTopModuleTest;
import Triton.SoccerObjects;

import java.util.TreeMap;

public class VirtualBotTestFactory {
    private final TreeMap<String, TritonTestable> virtualBotTestMap = new TreeMap<>();
    
    public VirtualBotTestFactory(SoccerObjects soccerObjects, Config config) {
        RobotList<Ally> fielders = soccerObjects.fielders;
        Ally keeper = soccerObjects.keeper;
        RobotList<Foe> foes = soccerObjects.foes;
        Ball ball = soccerObjects.ball;
        virtualBotTestMap.put("vmcutop", new VirtualMcuTopModuleTest(soccerObjects));
        virtualBotTestMap.put("grsim-client", new GrSimClientModuleTest());
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
