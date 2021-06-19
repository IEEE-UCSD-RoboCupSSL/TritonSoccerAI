package Triton.ManualTests.CoreTests.TestHelpers;

import Triton.Config.Config;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.TritonTestable;

import java.util.Scanner;

public class RotateAllRobots implements TritonTestable {
    private final RobotList<? extends Ally> allies;

    public RotateAllRobots(RobotList<? extends Ally> allies) {
        this.allies = allies;
    }

    @Override
    public boolean test(Config config) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter angle: ");

        String s = scanner.nextLine();

        try{
            int angle = Integer.parseInt(s);

            for (int i = 0; i < 100; i++) {

                for (Ally ally : allies) {
                    ally.rotateTo(angle);
                }
            }
        }catch (Exception e) {
            System.out.println("Invalid Input");
            return false;
        }
        return true;
    }
}
