package Triton.CoreModules.Robot;

import Triton.App;
import Triton.Config.ObjectConfig;

import java.util.concurrent.ThreadPoolExecutor;

public class RobotFactory {

    /* Don't include goalkeeper bot */
    public static RobotList<Ally> createAllyBots(int numRobots) {
        /* Instantiate & run Our Robots (Ally) modules */
        RobotList<Ally> allies = new RobotList<Ally>();
        for (int i = 0; i < numRobots; i++) {
            Ally ally = new Ally(ObjectConfig.MY_TEAM, i);
            allies.add(ally);
        }
        return allies;
    }

    /* GoalKeeper always uses our robot of the last ID */
    public static Ally createGoalKeeperBot() {
        return new Ally(ObjectConfig.MY_TEAM, ObjectConfig.ROBOT_COUNT - 1);
    }

    /* Do include the opponent goalkeeper */
    public static RobotList<Foe> createFoeBotsForTracking(int numRobots) {
        /* Instantiate & run Opponent Robots (Foe) modules */
        RobotList<Foe> foes = new RobotList<Foe>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            Team foeTeam = (ObjectConfig.MY_TEAM == Team.BLUE) ? Team.YELLOW : Team.BLUE;
            Foe foe = new Foe(foeTeam, i);
            foes.add(foe);
        }
        return foes;
    }

}
