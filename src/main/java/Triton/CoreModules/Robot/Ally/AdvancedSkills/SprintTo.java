package Triton.CoreModules.Robot.Ally.AdvancedSkills;

import Triton.Config.OldConfigs.PathfinderConfig;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.Misc.Math.Matrix.Vec2D;

import java.util.ArrayList;

import static Triton.Config.OldConfigs.PathfinderConfig.SPRINT_TO_ROTATE_DIST_THRESH;
import static Triton.Misc.Math.Coordinates.PerspectiveConverter.calcAngDiff;
import static Triton.Misc.Math.Coordinates.PerspectiveConverter.normAng;

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
                if (absAngleDiff <= PathfinderConfig.MOVE_ANGLE_THRESH) {
                    if (path.size() <= 2) ally.moveTo(nextNode);
                    else ally.moveToNoSlowDown(nextNode);
                } else {
                    ally.moveAt(new Vec2D(0, 0));
                }
            } else {
                if (!fastestAngleFound) ally.spinTo(normAng(fastestAngle));
                else ally.spinTo(normAng(fastestAngle + 180));

                if (absAngleDiff >= 180 - PathfinderConfig.MOVE_ANGLE_THRESH) {
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
