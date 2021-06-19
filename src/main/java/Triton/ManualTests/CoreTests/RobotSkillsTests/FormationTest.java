package Triton.ManualTests.CoreTests.RobotSkillsTests;

import Triton.Config.Config;
import Triton.CoreModules.AI.Formation;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.RobotList;

import java.time.LocalDateTime;
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
            LocalDateTime in10Secs = LocalDateTime.now().plusSeconds(10);
            while (!Formation.getInstance().moveToFormation(formationName, fielders, keeper)) {

                if (!LocalDateTime.now().isBefore(in10Secs)){
                    System.out.println("[Error] Moving to formation failed!");
                    return false;
                }

                delay(3);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
