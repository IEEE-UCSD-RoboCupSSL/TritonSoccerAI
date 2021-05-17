package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Scanner;

@NoArgsConstructor
@AllArgsConstructor
public class DynamicInterceptBallTest extends RobotSkillsTest {
    Ally ally;
    Ball ball;


    @Override
    public boolean test() {
        System.out.println("intercepting ball");
        ally.stop();
        while (true) {
            if(ally.isHoldingBall()) {
                ally.stop();
            } else {
                ally.dynamicIntercept(ball, 90.0);
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
