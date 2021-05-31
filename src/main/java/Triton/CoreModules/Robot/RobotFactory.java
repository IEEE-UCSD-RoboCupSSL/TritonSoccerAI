package Triton.CoreModules.Robot;

import Triton.Config.Config;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;

public class RobotFactory {

    /* Don't include goalkeeper bot */
    public static RobotList<Ally> createAllyFielderBots(Config config) {
        /* Instantiate & run Our Robots (Ally) modules */
        RobotList<Ally> allies = new RobotList<Ally>();
        for (int i = 0; i < config.connConfig.numRobots - 1; i++) {
            Ally ally = new Ally(config, i);
            allies.add(ally);
        }
        return allies;
    }

    /* GoalKeeper always uses our robot of the last ID */
    public static Ally createGoalKeeperBot(Config config) {
        return new Ally(config, config.connConfig.numRobots - 1);
    }

    /* Do include the opponent goalkeeper */
    public static RobotList<Foe> createFoeBotsForTracking(Config config) {
        /* Instantiate & run Opponent Robots (Foe) modules */
        RobotList<Foe> foes = new RobotList<Foe>();
        for (int i = 0; i < config.connConfig.numRobots; i++) {
            Foe foe = new Foe(config, i);
            foes.add(foe);
        }
        return foes;
    }

}
