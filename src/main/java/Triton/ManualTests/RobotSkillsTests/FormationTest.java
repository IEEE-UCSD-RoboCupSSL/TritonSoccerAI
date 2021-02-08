package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.AI.Formation;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.RobotList;

import java.util.Scanner;

public class FormationTest extends RobotSkillsTest {

    Scanner scanner;
    RobotList<Ally> fielders;
    Ally keeper;
    String formationName;

    public FormationTest(Scanner scanner, RobotList<Ally> fielders, Ally keeper) {
        this.scanner = scanner;
        this.fielders = fielders;
        this.keeper = keeper;
        this.formationName = null;
    }

    public FormationTest(Scanner scanner, RobotList<Ally> fielders) {
        this.scanner = scanner;
        this.fielders = fielders;
        this.keeper = null;
        this.formationName = null;
    }

    public FormationTest(String formationName, RobotList<Ally> fielders, Ally keeper) {
        this.scanner = null;
        this.fielders = fielders;
        this.keeper = keeper;
        this.formationName = formationName;
    }

    public FormationTest(String formationName, RobotList<Ally> fielders) {
        this.scanner = null;
        this.fielders = fielders;
        this.keeper = null;
        this.formationName = formationName;
    }

    @Override
    public boolean test() {
        if (scanner != null) {
            System.out.println(">> ENTER FORMATION TO MOVE TO:");
            formationName = scanner.nextLine();
        }

        while (!Formation.getInstance().moveToFormation(formationName, fielders, keeper)) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
