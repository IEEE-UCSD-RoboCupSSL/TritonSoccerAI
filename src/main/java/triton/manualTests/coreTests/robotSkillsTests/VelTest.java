package triton.manualTests.coreTests.robotSkillsTests;

import triton.config.Config;
import triton.coreModules.robot.ally.Ally;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.Scanner;

public class VelTest extends RobotSkillsTest {
    Ally ally;

    public VelTest( Ally ally) {
        this.ally = ally;
    }

    @Override
    public boolean test(Config config) {
        Scanner scanner = new Scanner(System.in);
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
