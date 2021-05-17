package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.AI.Formation;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.RobotList;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Scanner;

public class FormationTest extends RobotSkillsTest {

    RobotList<Ally> fielders;
    Ally keeper;
    String formationName;

    public FormationTest(RobotList<Ally> fielders, Ally keeper) {
        this.fielders = fielders;
        this.keeper = keeper;
        this.formationName = null;
    }

    public FormationTest(RobotList<Ally> fielders) {
        this.fielders = fielders;
        this.keeper = null;
        this.formationName = null;
    }

    public FormationTest(String formationName, RobotList<Ally> fielders, Ally keeper) {
        this.fielders = fielders;
        this.keeper = keeper;
        this.formationName = formationName;
    }

    public FormationTest(String formationName, RobotList<Ally> fielders) {
        this.fielders = fielders;
        this.keeper = null;
        this.formationName = formationName;
    }

    @Override
    public boolean test() {
        Scanner scanner = new Scanner(System.in);
        Formation.printAvailableFormations();
        System.out.println(">> ENTER FORMATION TO MOVE TO:");
        formationName = scanner.nextLine();

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
