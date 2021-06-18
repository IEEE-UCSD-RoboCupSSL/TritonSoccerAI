package Triton.ManualTests;

import Triton.Config.Config;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.RobotList;

public class StopAllRobots implements TritonTestable{
    private final RobotList<? extends Ally> allies;

    public StopAllRobots(RobotList<? extends Ally> allies) {
        this.allies = allies;
    }

    @Override
    public boolean test(Config config) {
        allies.stopAll();
        return true;
    }
}
