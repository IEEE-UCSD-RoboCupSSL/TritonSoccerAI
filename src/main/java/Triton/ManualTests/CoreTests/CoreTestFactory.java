package Triton.ManualTests.CoreTests;

import Triton.Config.Config;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.CoreTests.AI_SkillsTests.CoordinatedPassTest;
import Triton.ManualTests.CoreTests.AI_SkillsTests.DodgingTest;
import Triton.ManualTests.CoreTests.AI_SkillsTests.GroupToTest;
import Triton.ManualTests.CoreTests.AI_SkillsTests.ShootGoalTest;
import Triton.ManualTests.CoreTests.AI_StrategiesTests.Summer2021PlayTest;
import Triton.ManualTests.CoreTests.AI_TacticsTests.DefendPlanATest;
import Triton.ManualTests.CoreTests.AI_TacticsTests.GapGetBallTest;
import Triton.ManualTests.CoreTests.DijkstraTest.DijkstraTest;
import Triton.ManualTests.CoreTests.EstimatorTests.GapFinderTest;
import Triton.ManualTests.CoreTests.EstimatorTests.PassFinderTest;

import Triton.ManualTests.CoreTests.RobotSkillsTests.*;
import Triton.ManualTests.CoreTests.RobotSkillsTests.AsyncSkillsTests.SimpleProceduralSkillDemo;
import Triton.ManualTests.CoreTests.TestHelpers.RotateAllRobots;
import Triton.ManualTests.CoreTests.TestHelpers.StopAllRobots;
import Triton.ManualTests.TritonTestable;
import Triton.SoccerObjects;

import java.util.TreeMap;

/**
 * --> Writing New Tests <--
 * Manual Tests are now automatically registered when you put the test name and an instantiation
 * of the test into `TestMaps`.
 *
 * Below are inputs needed by the tests. If a new test requires some more inputs simply overload
 * the constructor.
 *
 */
public class CoreTestFactory {
    private final TreeMap<String, TritonTestable> coreTestMap = new TreeMap<>();


    /**
     * Constructor created to accommodate existing tests at the time of refactoring.
     *
     */
    public CoreTestFactory(SoccerObjects soccerObjects, Config config) {
        RobotList<Ally> fielders = soccerObjects.fielders;
        Ally keeper = soccerObjects.keeper;
        RobotList<Foe> foes = soccerObjects.foes;
        Ball ball = soccerObjects.ball;
        coreTestMap.put("pmotion", new PrimitiveMotionTest(fielders.get(3)));
        coreTestMap.put("amotion", new AdvancedMotionTest(fielders.get(1)));
        coreTestMap.put("getball", new GetBallTest(fielders.get(3), ball));
        coreTestMap.put("kick", new KickTest(fielders.get(3), ball));
        coreTestMap.put("misc", new MiscTest(fielders.get(3), ball));
        coreTestMap.put("cpass", new CoordinatedPassTest(fielders, keeper, foes, ball));
        coreTestMap.put("group", new GroupToTest(fielders, ball));
        coreTestMap.put("drib", new DribBallTest(fielders.get(1), ball));
        coreTestMap.put("vel", new VelTest(fielders.get(0)));
        coreTestMap.put("inter", new FlankBallTest(fielders.get(1), ball));
        coreTestMap.put("collect", new DataCollector(fielders, keeper, ball));
        coreTestMap.put("reset", new FormationTest("tester", fielders));
        coreTestMap.put("formation", new FormationTest(fielders, keeper));
        coreTestMap.put("gap", new GapFinderTest(fielders, foes, ball));
        coreTestMap.put("pass", new PassFinderTest(fielders, foes, ball));
        coreTestMap.put("gapgetball", new GapGetBallTest(fielders, keeper, foes, ball));
        coreTestMap.put("shoot", new ShootGoalTest(fielders.get(0), foes, ball));
        coreTestMap.put("keep", new KeeperTest(fielders, keeper, foes, ball));
        coreTestMap.put("defendA", new DefendPlanATest(fielders, keeper, foes, ball));
        coreTestMap.put("dodge", new DodgingTest(fielders, keeper, foes, ball));
        coreTestMap.put("holdballpos", new HoldBallPosTest(fielders.get(3), ball));
        coreTestMap.put("play", new Summer2021PlayTest(fielders, keeper, foes, ball));
        coreTestMap.put("capDetect", new CapDetectionTest(fielders.get(3), ball));
        coreTestMap.put("spsdemo", new SimpleProceduralSkillDemo(fielders, ball));
        coreTestMap.put("defaultFormation", new FormationTest("tester", fielders));
        coreTestMap.put("autocap", new AutoCapTest(fielders.get(3), ball));
        coreTestMap.put("rotateAll", new RotateAllRobots(fielders));
        coreTestMap.put("stopAll", new StopAllRobots(fielders));
        coreTestMap.put("dijkstra-test", new DijkstraTest(soccerObjects));
    }

    public String[] getAvailableTestNames() {
        return coreTestMap.keySet().toArray(new String[0]);
    }

    public void printAvailableTestNames() {
        System.out.println("Available Tests:");
        for (String test : coreTestMap.keySet()) {
            System.out.printf("- %s \n", test);
        }
        System.out.println();
    }

    public TritonTestable getTest(String testName) {
        return coreTestMap.get(testName);
    }
}
