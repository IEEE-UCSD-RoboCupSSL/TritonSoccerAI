package Triton.CoreModules.AI.GameProcedures;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.Misc.Math.Matrix.Vec2D;

import static Triton.Config.ObjectConfig.DRIBBLER_OFFSET;

public class BallPlacement {

    public static boolean placeBall(Ally ally, Ball ball, Vec2D targetPos) {
        if (ball.isPosArrived(targetPos)) {
            if (ally.isHoldingBall()) {
                ally.kick(new Vec2D(0.1, 0.1));
                return true;
            }
        } else {
            if (ally.isHoldingBall()) {
                ally.curveTo(targetPos.add(new Vec2D(0, -DRIBBLER_OFFSET)), 0);
            } else {
                ally.getBall(ball);
            }
        }
        return false;
    }
}
