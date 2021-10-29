package triton.manualTests.coreTests.testHelpers;

import triton.config.Config;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.RobotList;
import triton.manualTests.TritonTestable;

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
