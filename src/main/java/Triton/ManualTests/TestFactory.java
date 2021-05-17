package Triton.ManualTests;

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
import Triton.ManualTests.RobotSkillsTests.*;
import Triton.ManualTests.RobotSkillsTests.AsyncSkillsTests.SimpleProceduralSkillDemo;

import java.util.HashMap;

public class TestFactory {
    RobotList<Ally> fielders;
    Ally keeper;
    RobotList<Foe> foes;
    Ball ball;

    HashMap<String, TritonTestable> testMap = new HashMap<>();

    public TestFactory(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {
        this.fielders = fielders;
        this.keeper = keeper;
        this.foes = foes;
        this.ball = ball;

        testMap.put("pmotion", new PrimitiveMotionTest(fielders.get(3)));
        testMap.put("amotion", new AdvancedMotionTest(fielders.get(1)));
        testMap.put("getball", new GetBallTest(fielders.get(3), ball));
        testMap.put("kick", new KickTest(fielders.get(3), ball));
        testMap.put("misc", new MiscTest(fielders.get(3), ball));
        testMap.put("cpass", new CPassTest(fielders, keeper, foes, ball));
        testMap.put("group", new GroupToTest(fielders, ball));
        testMap.put("drib", new DribBallTest(fielders.get(1), ball));
        testMap.put("vel", new VelTest(fielders.get(0)));
        testMap.put("inter", new DynamicInterceptBallTest(fielders.get(1), ball));
        testMap.put("collect", new DataCollector(fielders, keeper, ball));
        testMap.put("reset", new FormationTest("tester", fielders));
        testMap.put("formation", new FormationTest(fielders, keeper));
        testMap.put("gap", new GapFinderTest(fielders, foes, ball));
        testMap.put("pass", new PassFinderTest(fielders, foes, ball));
        testMap.put("gapgetball", new GapGetBallTest(fielders, keeper, foes, ball));
        testMap.put("shoot", new ShootGoalTest(fielders.get(0), foes, ball));
        testMap.put("keep", new KeeperTest(fielders, keeper, foes, ball));
        testMap.put("defendA", new DefendPlanATest(fielders, keeper, foes, ball));
        testMap.put("dodge", new DodgingTest(fielders, keeper, foes, ball));
        testMap.put("holdballpos", new HoldBallPosTest(fielders.get(3), ball));
        testMap.put("basicplay", new BasicPlayTest(fielders, keeper, foes, ball));
        testMap.put("capDetect", new CapDetectionTest(fielders.get(3), ball));
        testMap.put("spsdemo", new SimpleProceduralSkillDemo(fielders, ball));
        testMap.put("defaultFormation", new FormationTest("tester", fielders));
    }

    public String[] getAvailableTestNames() {
        return testMap.keySet().toArray(new String[0]);
    }

    public void printAvailableTestNames() {
        int counter = 0;
        System.out.println("Available Tests:");
        for (String test : testMap.keySet()) {
            System.out.printf("%d. %s \n", counter++, test);
        }
        System.out.println("");
    }

    public TritonTestable getTest(String testName) {
        return testMap.get(testName);
    }


}
