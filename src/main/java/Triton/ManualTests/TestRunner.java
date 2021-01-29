package Triton.ManualTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.RobotSkillsTests.PrimitiveMotionTest;
import Triton.ManualTests.RobotSkillsTests.RobotSkillsTest;
import Triton.Misc.ModulePubSubSystem.Module;

public class TestRunner implements Module {

    private final String testName;
    private final RobotList<Ally> allies;
    private final Ally keeper;
    private final RobotList<Foe> foes;
    private final Ball ball;
    public TestRunner(String testName, RobotList<Ally> allies,
                      Ally keeper, RobotList<Foe> foes, Ball ball) {
        this.allies = allies;
        this.keeper = keeper;
        this.foes = foes;
        this.ball = ball;
        this.testName = testName;
    }


    @Override
    public void run() {
        switch (testName) {
            case "PrimitiveMotion"-> (new PrimitiveMotionTest(allies.get(0))).test();
            case "..." -> System.out.println("...");
            default -> {
                System.out.println("Invalid Test Name");
            }
        }
    }
}
