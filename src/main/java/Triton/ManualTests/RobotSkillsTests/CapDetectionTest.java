package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.Misc.Math.Matrix.Vec2D;

import java.time.LocalTime;
import java.util.Scanner;

public class CapDetectionTest extends RobotSkillsTest{
    private final Scanner scanner;
    private final Ally ally;
    private final Ball ball;

    public CapDetectionTest(Scanner scanner, Ally ally, Ball ball) {
        this.scanner = scanner;
        this.ally = ally;
        this.ball = ball;
    }


    @Override
    public boolean test() {
        LocalTime then;

        while(true){
            System.out.println("getting ball..");
            while (!ally.isHoldingBall()) {
                ally.getBall(ball);
            }

            ally.stop();

            then = LocalTime.now().plusSeconds(10);

            while(LocalTime.now().isBefore(then)) {
                ally.spinAt(30);
                ally.moveAt(new Vec2D(25, 0));
                System.out.println(ally.isHoldingBall());
            }

            System.out.println(">> Available Actions: 1) quit   2) rerun");
            System.out.println(">> ENTER ACTION:");
            String line = scanner.nextLine();

            if(line.equalsIgnoreCase("quit")) {
                break;
            }
        }
        return true;
    }
}
