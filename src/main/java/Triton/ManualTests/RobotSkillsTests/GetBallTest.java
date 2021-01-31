package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;

import java.util.Scanner;

public class GetBallTest extends RobotSkillsTest {
    Scanner scanner;
    Ally bot;
    Ball ball;

    public GetBallTest(Scanner scanner, Ally bot, Ball ball) {
        this.scanner = scanner;
        this.bot = bot;
        this.ball = ball;
    }

    @Override
    public boolean test() {
//        String line;
//        do {
//            System.out.println(">> ENTER \"start\" TO START:");
//            line = scanner.nextLine();
//        } while (!line.equals("start"));

        while (!bot.isHoldingBall()) {
            bot.getBall(ball);
        }

        bot.stop();

        return true;
    }
}
