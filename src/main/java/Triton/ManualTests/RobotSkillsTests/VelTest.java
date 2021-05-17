package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Robot.Ally.Ally;
import Triton.Misc.Math.Matrix.Vec2D;

import java.util.Scanner;

public class VelTest extends RobotSkillsTest {
    Scanner scanner;
    Ally ally;

    public VelTest(Scanner scanner, Ally ally) {
        this.scanner = scanner;
        this.ally = ally;
    }

    @Override
    public boolean test() {
        try {
            System.out.println(">> MOVE TO WHERE?:");
            double posX = scanner.nextDouble();
            double posY = scanner.nextDouble();
            scanner.nextLine();

            Vec2D targetPos = new Vec2D(posX, posY);
            while (!ally.isPosArrived(targetPos)) {
                ally.curveTo(targetPos);
                System.out.println(ally.getVel());
            }

            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }
}
