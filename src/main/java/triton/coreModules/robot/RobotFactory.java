package triton.coreModules.robot;

import triton.config.Config;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;

public class RobotFactory {

    /* Not including goalkeeper bot */
    public static RobotList<Ally> createAllyFielderBots(Config config) {
        /* Instantiate & run Our Robots (Ally) modules */
        RobotList<Ally> allies = new RobotList<Ally>();
        for (int id = 0; id < config.numAllyRobots - 1; id++) {
            Ally ally = new Ally(config, id);
            allies.add(ally);
        }
        return allies;
    }

    /* GoalKeeper always uses our robot of the last ID */
    public static Ally createGoalKeeperBot(Config config) {
        return new Ally(config, config.numAllyRobots - 1);
    }

    /* Do include the opponent goalkeeper */
    public static RobotList<Foe> createFoeBotsForTracking(Config config) {
        /* Instantiate & run Opponent Robots (Foe) modules */
        RobotList<Foe> foes = new RobotList<Foe>();
        for (int id = 0; id < config.numAllyRobots; id++) {
            Foe foe = new Foe(config, id);
            foes.add(foe);
        }
        return foes;
    }

}
