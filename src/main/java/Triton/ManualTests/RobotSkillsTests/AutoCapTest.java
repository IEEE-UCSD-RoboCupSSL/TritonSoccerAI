package Triton.ManualTests.RobotSkillsTests;

import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcPathfinder;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.ManualTests.TritonTestable;
import Triton.Misc.Math.Matrix.Vec2D;

public class AutoCapTest implements TritonTestable {

    private final Ally ally;
    private final Ball ball;

    public AutoCapTest(Ally ally, Ball ball) {
        this.ally = ally;
        this.ball = ball;
    }

    @Override
    public boolean test(Config config) {
        Vec2D ballLoc = ball.getPos();
        Vec2D currPos = ally.getPos();
        Vec2D currPosToBall = ballLoc.sub(currPos);
        while(!ally.isHoldingBall()) {
            ally.autoCap();
        }
        return true;
    }
}
