package triton.manualTests.coreTests;

import triton.config.Config;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.RobotList;
import triton.manualTests.coreTests.aiSkillsTests.*;
import triton.manualTests.coreTests.aiSkillsTests.CoordinatedPassTest;
import triton.manualTests.coreTests.aiSkillsTests.DodgingTest;
import triton.manualTests.coreTests.aiSkillsTests.GroupToTest;
import triton.manualTests.coreTests.aiSkillsTests.ShootGoalTest;
import triton.manualTests.coreTests.aiTacticsTests.AttackPlan2021Test;
import triton.manualTests.coreTests.aiStrategiesTests.Summer2021PlayTest;
import triton.manualTests.coreTests.aiTacticsTests.DefendPlanATest;
import triton.manualTests.coreTests.aiTacticsTests.GapGetBallTest;
import triton.manualTests.coreTests.dijkstraTest.DijkstraTest;
import triton.manualTests.coreTests.estimatorTests.AttackSupportMapTest;
import triton.manualTests.coreTests.estimatorTests.PassProbMapTest;


import triton.manualTests.coreTests.robotSkillsTests.*;
import triton.manualTests.coreTests.robotSkillsTests.asyncSkillsTests.SimpleProceduralSkillDemo;
import triton.manualTests.coreTests.testHelpers.RotateAllRobots;
import triton.manualTests.coreTests.testHelpers.StopAllRobots;
import triton.manualTests.TritonTestable;
import triton.SoccerObjects;

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
        coreTestMap.put("atks-map", new AttackSupportMapTest(fielders, foes, ball));
        coreTestMap.put("pass-map", new PassProbMapTest(fielders, foes, ball));
        coreTestMap.put("path", new PathFinderTest(fielders, keeper, ball));
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
        coreTestMap.put("attack2021", new AttackPlan2021Test(fielders, keeper, foes, ball, config));
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
