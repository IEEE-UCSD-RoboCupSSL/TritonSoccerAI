package triton.manualTests.coreTests.aiSkillsTests;

import triton.config.Config;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.RobotList;
import triton.manualTests.TritonTestable;

public class PathFinderTest implements TritonTestable {

    RobotList<Ally> fielders;
    Ally keeper;
    Ball ball;

    public PathFinderTest(RobotList<Ally> fielders, Ally keeper, Ball ball) {
        this.ball = ball;
        this.fielders = fielders;
        this.keeper = keeper;
    }

    public boolean test(Config config) {
        // this.fielders.get(4).displayPathFinder();
        this.keeper.displayPathFinder();
        return true;
    }
}
