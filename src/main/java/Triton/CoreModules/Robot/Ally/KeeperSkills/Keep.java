package Triton.CoreModules.Robot.Ally.KeeperSkills;

import Triton.Config.PathfinderConfig;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.Misc.Math.Matrix.Vec2D;

import static Triton.Config.GeometryConfig.*;

public class Keep {

    public static void exec(Ally ally, Ball ball, Vec2D aimTraj) {
        Vec2D currPos = ally.getPos();
        Vec2D ballPos = ball.getPos();
        double y = -FIELD_LENGTH / 2 + 150;

        if (currPos.sub(ballPos).mag() < PathfinderConfig.AUTOCAP_DIST_THRESH) {
            ally.getBall(ball);
        } else {
            double x;
            if (Math.abs(aimTraj.y) <= 0.0001 || Math.abs(aimTraj.x) <= 0.0001) {
                x = ballPos.x;
            } else {
                double m = aimTraj.y / aimTraj.x;
                double b = ballPos.y - (ballPos.x * m);
                x = (y - b) / m;
            }

            x = Math.max(x, GOAL_LEFT);
            x = Math.min(x, GOAL_RIGHT);
            Vec2D targetPos = new Vec2D(x, y);
            ally.fastCurveTo(targetPos);
        }
    }
}
