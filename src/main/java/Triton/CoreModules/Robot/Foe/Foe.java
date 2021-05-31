package Triton.CoreModules.Robot.Foe;

import Triton.Config.Config;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.Team;

public class Foe extends Robot {

    public Foe(Config config, int ID) {
        super(config.foeTeam, ID);
    }
}