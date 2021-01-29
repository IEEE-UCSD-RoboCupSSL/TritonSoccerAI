package Triton.ManualTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.RobotSkillsTests.GetBallTest;
import Triton.ManualTests.RobotSkillsTests.KickTest;
import Triton.ManualTests.RobotSkillsTests.PrimitiveMotionTest;
import Triton.Misc.ModulePubSubSystem.Module;

import java.util.Scanner;

public class TestRunner implements Module {

    private final Scanner scanner;
    private final RobotList<Ally> allies;
    private final Ally keeper;
    private final RobotList<Foe> foes;
    private final Ball ball;

    private String testName;

    public TestRunner(RobotList<Ally> allies,
                      Ally keeper, RobotList<Foe> foes, Ball ball) {
        this.allies = allies;
        this.keeper = keeper;
        this.foes = foes;
        this.ball = ball;

        scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);

            boolean quit = false;
            while (!quit) {
                System.out.println(">> ENTER TEST NAME:");
                testName = scanner.nextLine();

                boolean rtn = false;
                switch (testName) {
                    case "PrimitiveMotion" -> rtn = new PrimitiveMotionTest(allies.get(0)).test();
                    case "GetBall" -> rtn = new GetBallTest(scanner, allies.get(0)).test();
                    case "Kick" -> rtn = new KickTest(scanner, allies.get(0)).test();
                    case "Quit" -> quit = true;
                    default -> System.out.println("Invalid Test Name");
                }

                System.out.println(rtn ? "Test Success" : "Test Fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
