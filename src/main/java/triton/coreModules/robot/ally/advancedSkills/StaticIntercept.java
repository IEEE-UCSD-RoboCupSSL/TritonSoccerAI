package triton.coreModules.robot.ally.advancedSkills;

import triton.config.globalVariblesAndConstants.GvcPathfinder;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.misc.math.linearAlgebra.Vec2D;

import static triton.misc.math.coordinates.PerspectiveConverter.normAng;

public class StaticIntercept {
    public static void exec(Ally ally, Ball ball, Vec2D anchorPos) {
        // To-do (future) : edge case: ball going opposite dir

        Vec2D currPos = ally.getPos();
        Vec2D ballPos = ball.getPos();
        Vec2D ballVelDir = ball.getVel().normalized();
        Vec2D ballToAnchor = anchorPos.sub(ballPos);
        Vec2D receivePoint = ballPos.add(ballVelDir.scale(ballToAnchor.dot(ballVelDir)));

        if (currPos.sub(ballPos).mag() < GvcPathfinder.AUTOCAP_DIST_THRESH) {
            ally.getBall(ball);
        } else {
            if (ball.getVel().mag() < 750) { // To-do: magic number && comment vel unit
                ally.getBall(ball);
            } else {
                ally.strafeTo(receivePoint, normAng(ballVelDir.toPlayerAngle() + 180));
            }
        }
    }
}
