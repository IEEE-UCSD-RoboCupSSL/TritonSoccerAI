package Triton.CoreModules.Robot.Ally.AdvancedSkills;

import Triton.Config.PathfinderConfig;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.Misc.Math.Matrix.Vec2D;

public class GetBall {
    public static void exec(Ally ally, Ball ball) {
        Vec2D ballLoc = ball.getPos();
        Vec2D currPos = ally.getPos();
        Vec2D currPosToBall = ballLoc.sub(currPos);
        if (currPosToBall.mag() <= PathfinderConfig.AUTOCAP_DIST_THRESH) {
            ally.autoCap();
        } else {
            ally.fastCurveTo(ballLoc, currPosToBall.toPlayerAngle());
            //dynamicIntercept(ball, 0);
        }
    }
}
