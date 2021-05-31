package Triton.CoreModules.Robot.Ally.AdvancedSkills;

import Triton.Config.GlobalVariblesAndConstants.GvcPathfinder;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.Misc.Math.Matrix.Vec2D;

import static Triton.Config.GlobalVariblesAndConstants.GvcAI.HOLDING_BALL_VEL_THRESH;
import static Triton.Misc.Math.Coordinates.PerspectiveConverter.calcAngDiff;
import static Triton.Misc.Math.Coordinates.PerspectiveConverter.normAng;

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
