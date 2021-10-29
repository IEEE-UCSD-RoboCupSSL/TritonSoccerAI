package triton.coreModules.robot.ally.advancedSkills;

import triton.config.globalVariblesAndConstants.GvcPathfinder;
import triton.coreModules.robot.ally.Ally;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.ArrayList;

import static triton.config.globalVariblesAndConstants.GvcAI.HOLDING_BALL_VEL_THRESH;
import static triton.misc.math.coordinates.PerspectiveConverter.calcAngDiff;
import static triton.misc.math.coordinates.PerspectiveConverter.normAng;

public class StrafeTo {
    public static void exec(Ally ally, Vec2D endPoint) {
        exec(ally, endPoint, ally.getDir());
    }

    public static void exec(Ally ally, Vec2D endPoint, double angle) {
        double targetAngle = normAng(angle);
        double angDiff = calcAngDiff(targetAngle, ally.getDir());
        double absAngleDiff = Math.abs(angDiff);

        if (absAngleDiff <= GvcPathfinder.RD_ANGLE_THRESH) {
            ally.spinTo(targetAngle);
        } else {
            ally.spinAt((ally.isHoldingBall()) ? Math.signum(angDiff) * HOLDING_BALL_VEL_THRESH : Math.signum(angDiff) * 100);
        }

        if (absAngleDiff <= GvcPathfinder.MOVE_ANGLE_THRESH) {
            ArrayList<Vec2D> path = ally.findPath(endPoint);
            if (path != null && path.size() > 0) {
                Vec2D nextNode;
                if (path.size() == 1) nextNode = path.get(0);
                else if (path.size() == 2) nextNode = path.get(1);
                else nextNode = path.get(1);

                if (path.size() <= 2) ally.moveTo(nextNode);
                else ally.moveToNoSlowDown(nextNode);
            } else {
                ally.moveAt(new Vec2D(0, 0));
            }
        } else {
            ally.moveAt(new Vec2D(0, 0));
        }
    }
}
