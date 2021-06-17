package Triton.CoreModules.Robot.Ally.AdvancedSkills;

import Triton.CoreModules.Robot.Ally.Ally;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

import java.util.ArrayList;

import static Triton.Misc.Math.Coordinates.PerspectiveConverter.calcAngDiff;
import static Triton.Misc.Math.Coordinates.PerspectiveConverter.normAng;

public class CurveTo {
    public static void exec(Ally ally, Vec2D endPoint) {
        exec(ally, endPoint, ally.getDir());
    }

    public static void exec(Ally ally, Vec2D endPoint, double angle) {
        double targetAngle = normAng(angle);
        double angDiff = calcAngDiff(targetAngle, ally.getDir());
        double absAngleDiff = Math.abs(angDiff);

        ally.spinTo(targetAngle);

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
    }
}
