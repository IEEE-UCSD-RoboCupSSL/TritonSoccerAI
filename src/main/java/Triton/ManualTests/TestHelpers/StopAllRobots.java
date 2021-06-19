package Triton.ManualTests.TestHelpers;

import Triton.Config.Config;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.TritonTestable;

public class StopAllRobots implements TritonTestable {
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
