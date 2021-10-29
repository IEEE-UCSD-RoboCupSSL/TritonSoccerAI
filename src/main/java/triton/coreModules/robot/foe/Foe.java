package triton.coreModules.robot.foe;

import triton.config.Config;
import triton.coreModules.robot.Robot;

public class Foe extends Robot {

    public Foe(Config config, int ID) {
        super(config.foeTeam, ID);
    }
}