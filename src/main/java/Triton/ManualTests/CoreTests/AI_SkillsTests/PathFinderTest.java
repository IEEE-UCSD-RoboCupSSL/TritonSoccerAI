package Triton.ManualTests.CoreTests.AI_SkillsTests;

import Triton.Config.Config;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.TritonTestable;

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
