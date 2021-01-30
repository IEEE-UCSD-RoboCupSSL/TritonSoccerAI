package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Robot.Ally;

import java.util.Scanner;

public class GetBallTest extends RobotSkillsTest {
    Scanner scanner;
    Ally bot;

    public GetBallTest(Scanner scanner, Ally bot) {
        this.scanner = scanner;
        this.bot = bot;
    }

    @Override
    public boolean test() {
//        String line;
//        do {
//            System.out.println(">> ENTER \"start\" TO START:");
//            line = scanner.nextLine();
//        } while (!line.equals("start"));

        while (!bot.isHoldingBall()) {
            bot.getBall();
        }

        bot.stop();

        return true;
    }
}
