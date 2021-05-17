package Triton.CoreModules.Robot.Ally.AdvancedSkills;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.Misc.Math.Matrix.Vec2D;

import static Triton.Config.PathfinderConfig.*;

public class DribRotate {
    public static boolean exec(Ally ally, Ball ball, double angle) {
        return exec(ally, ball, angle, 0);
    }

    public static boolean exec(Ally ally, Ball ball, double angle, double offsetDist) {
        // POSITION
        // Unit vector from final end point to the ball
        Vec2D angleUnitDir = new Vec2D(angle);
        // Scale angleUnitDir to have a certain distance from the ball
        Vec2D angleOffsetVec = angleUnitDir.scale(DRIB_ROTATE_DIST + offsetDist);
        // Subtract the offset from the current ball location to determine the final location of the robot
        Vec2D endPos = ball.getPos().sub(angleOffsetVec);

        // Vector from ball to robot
        Vec2D ballToBot = ally.getPos().sub(ball.getPos());
        // Unit vector from ball to robot
        Vec2D ballPushDir = ballToBot.normalized();
        // Distance from ball to robot
        double ballToBotDist = ballToBot.mag();
        // Difference between the current rotation and the final rotation
        double angleDiff = (angleUnitDir.dot(ballPushDir) - 1.0) / 2.0;
        // Calculate the push from the ball to move the robot away from the ball
        // This push will get weaker as the robot rotates around the ball
        // The push will be 0 when the robot's position is in alignment with the end position
        Vec2D ballPushVec = ballPushDir.scale(DRIB_ROTATE_BALL_PUSH / ballToBotDist).scale(angleDiff);

        // Add the end position and the push to get the target position of the robot
        Vec2D targetPos = endPos.add(ballPushVec);

        // ANGLE
        // Angle from the bot to the ball
        double botToBallAngle = ball.getPos().sub(ally.getPos()).normalized().toPlayerAngle();
        // Smoothly translate between botToBallAngle vs end angle using the distance from the bot to the final
        // destination

        // Distance from current location of robot to the end destination
        double distToEnd = endPos.sub(ally.getPos()).mag();
        // Divide by the max distance the robot can be from the destination
        double distToEndScale = distToEnd / (DRIB_ROTATE_MAX_DIST + 2 * offsetDist) * 2;

        double targetAngle = (distToEndScale * botToBallAngle + (2 - distToEndScale) * angle) / 2;


        ally.curveTo(targetPos, angle);

        if (ball.getPos().sub(ally.getPos()).mag() - DRIB_ROTATE_DIST > 30) {
            return false;
        }
        return true;
    }
}
