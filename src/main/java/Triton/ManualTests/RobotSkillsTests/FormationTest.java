package Triton.ManualTests.RobotSkillsTests;

import Triton.Config.Config;
import Triton.CoreModules.AI.Formation;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.RobotList;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Scanner;

import static Triton.Util.delay;

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
    public boolean test(Config config) {
        Scanner scanner = new Scanner(System.in);
        Formation.printAvailableFormations();
        System.out.println(">> ENTER FORMATION TO MOVE TO:");
        formationName = scanner.nextLine();

        try {
            while (!Formation.getInstance().moveToFormation(formationName, fielders, keeper)) {
                delay(3);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
