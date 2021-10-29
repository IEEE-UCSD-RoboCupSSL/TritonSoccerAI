package triton.coreModules.robot.ally.advancedSkills;

import triton.coreModules.robot.ally.Ally;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.ArrayList;

import static triton.config.globalVariblesAndConstants.GvcPathfinder.SPRINT_TO_ROTATE_DIST_THRESH;
import static triton.misc.math.coordinates.PerspectiveConverter.calcAngDiff;
import static triton.misc.math.coordinates.PerspectiveConverter.normAng;

public class FastCurveTo {
    public static void exec(Ally ally, Vec2D endPoint) {
        exec(ally, endPoint, ally.getDir());
    }

    public static void exec(Ally ally, Vec2D endPoint, double endAngle) {
        // included rear-prioritizing case
        ArrayList<Vec2D> path = ally.findPath(endPoint);
        if (path != null && path.size() > 0) {
            double fastestAngle = endPoint.sub(ally.getPos()).toPlayerAngle();
            Vec2D nextNode;
            if (path.size() == 1) nextNode = path.get(0);
            else if (path.size() == 2) nextNode = path.get(1);
            else nextNode = path.get(1);

            double angDiff = calcAngDiff(fastestAngle, ally.getDir());
            double absAngleDiff = Math.abs(angDiff);

            if (endPoint.sub(ally.getPos()).mag() < SPRINT_TO_ROTATE_DIST_THRESH) {
                ally.spinTo(endAngle);
            } else {
                if (absAngleDiff <= 90) ally.spinTo(fastestAngle);
                else ally.spinTo(normAng(fastestAngle + 180));
            }
            if (path.size() <= 2) ally.moveTo(nextNode);
            else ally.moveToNoSlowDown(nextNode);
        } else {
            ally.moveAt(new Vec2D(0, 0));
            ally.spinAt(0);
        }
    }
}
