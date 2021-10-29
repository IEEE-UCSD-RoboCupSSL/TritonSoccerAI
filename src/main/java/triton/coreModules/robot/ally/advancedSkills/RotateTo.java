package triton.coreModules.robot.ally.advancedSkills;

import triton.config.globalVariblesAndConstants.GvcPathfinder;
import triton.coreModules.robot.ally.Ally;
import triton.misc.math.linearAlgebra.Vec2D;

import static triton.config.globalVariblesAndConstants.GvcAI.HOLDING_BALL_VEL_THRESH;
import static triton.misc.math.coordinates.PerspectiveConverter.calcAngDiff;
import static triton.misc.math.coordinates.PerspectiveConverter.normAng;

public class RotateTo {
    public static void exec(Ally ally, double angle) {
        double targetAngle = normAng(angle);
        double angDiff = calcAngDiff(targetAngle, ally.getDir());

        double absAngleDiff = Math.abs(angDiff);
        if (absAngleDiff <= GvcPathfinder.RD_ANGLE_THRESH) {
            ally.moveAt(new Vec2D(0, 0));
            ally.spinTo(targetAngle);
        } else {
            ally.moveAt(new Vec2D(0, 0));
            ally.spinAt((ally.isHoldingBall()) ? Math.signum(angDiff) * HOLDING_BALL_VEL_THRESH : Math.signum(angDiff) * 100);
        }
    }
}
