package triton.coreModules.robot.ally.advancedSkills;

import triton.config.globalVariblesAndConstants.GvcPathfinder;
import triton.coreModules.robot.ally.Ally;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.ArrayList;

import static triton.config.globalVariblesAndConstants.GvcPathfinder.SPRINT_TO_ROTATE_DIST_THRESH;
import static triton.misc.math.coordinates.PerspectiveConverter.calcAngDiff;
import static triton.misc.math.coordinates.PerspectiveConverter.normAng;

public class SprintTo {
    public static void exec(Ally ally, Vec2D endPoint) {
        exec(ally, endPoint, ally.getDir());
    }

    public static void exec(Ally ally, Vec2D endPoint, double endAngle) {
        // included rear-prioritizing case
        ArrayList<Vec2D> path = ally.findPath(endPoint);
        if (path != null && path.size() > 0) {
            double fastestAngle = 0;
            boolean fastestAngleFound = false;
            for (Vec2D node : path) {
                double dist = node.sub(ally.getPos()).mag();
                if (dist >= SPRINT_TO_ROTATE_DIST_THRESH) {
                    fastestAngle = node.sub(ally.getPos()).toPlayerAngle();
                    fastestAngleFound = true;
                    break;
                }
            }

            Vec2D nextNode;
            if (path.size() == 1) nextNode = path.get(0);
            else if (path.size() == 2) nextNode = path.get(1);
            else nextNode = path.get(1);

            if (!fastestAngleFound) fastestAngle = endAngle;

            double angDiff = calcAngDiff(fastestAngle, ally.getDir());
            double absAngleDiff = Math.abs(angDiff);

            if (absAngleDiff <= 90) {
                ally.spinTo(fastestAngle);
                if (absAngleDiff <= GvcPathfinder.MOVE_ANGLE_THRESH) {
                    if (path.size() <= 2) ally.moveTo(nextNode);
                    else ally.moveToNoSlowDown(nextNode);
                } else {
                    ally.moveAt(new Vec2D(0, 0));
                }
            } else {
                if (!fastestAngleFound) ally.spinTo(normAng(fastestAngle));
                else ally.spinTo(normAng(fastestAngle + 180));

                if (absAngleDiff >= 180 - GvcPathfinder.MOVE_ANGLE_THRESH) {
                    if (path.size() <= 2) ally.moveTo(nextNode);
                    else ally.moveToNoSlowDown(nextNode);
                } else {
                    ally.moveAt(new Vec2D(0, 0));
                }
            }
        } else {
            ally.moveAt(new Vec2D(0, 0));
            ally.spinAt(0);
        }
    }
}
