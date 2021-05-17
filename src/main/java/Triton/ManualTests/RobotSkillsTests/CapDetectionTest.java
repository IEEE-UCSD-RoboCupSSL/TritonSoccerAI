package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.Misc.Math.Matrix.Vec2D;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Scanner;


@AllArgsConstructor
public class CapDetectionTest extends RobotSkillsTest{
    private Ally ally;
    private Ball ball;


    @Override
    public boolean test() {
        Scanner scanner = new Scanner(System.in);
        LocalTime then;
        int spinRate;
        int moveRate;

        while(true){
            try {
                System.out.println(">> Enter spin rate: ");
                spinRate = Integer.parseInt(scanner.nextLine());
                System.out.println(">> Enter move rate: ");
                moveRate = Integer.parseInt(scanner.nextLine());
            }catch (NumberFormatException e){
                System.out.println("Please make sure the input is valid!");
                continue;
            }

            System.out.println("getting ball..");
            while (!ally.isHoldingBall()) {
                ally.getBall(ball);
            }

            ally.stop();

            then = LocalTime.now().plusSeconds(10);

            while(LocalTime.now().isBefore(then)) {
                ally.spinAt(spinRate);
                ally.moveAt(new Vec2D(moveRate, 0));
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
