package triton.misc.math.coordinates;

import triton.coreModules.robot.Side;
import triton.misc.math.linearAlgebra.Vec2D;

public class PerspectiveConverter {
    private static Side mySide = Side.GoalToGuardAtLeft; // default
    public static void setSide(Side team) {
        PerspectiveConverter.mySide = team;
    }
    
    public static Vec2D audienceToPlayer(Vec2D audienceVector) {
        if (mySide == Side.GoalToGuardAtLeft) {
            return new Vec2D(-audienceVector.y, audienceVector.x);
        } else {
            return new Vec2D(audienceVector.y, -audienceVector.x);
        }
    }

    public static int[] audienceToPlayer(int[] audienceVector) {
        if (mySide == Side.GoalToGuardAtLeft) {
            return new int[]{-audienceVector[1], audienceVector[0]};
        } else {
            return new int[]{audienceVector[1], -audienceVector[0]};
        }
    }

    public static double audienceToPlayer(double audienceAngle) {
        if (mySide == Side.GoalToGuardAtLeft) {
            return audienceAngle;
        } else {
            return normAng(audienceAngle + 180);
        }
    }

    public static double normAng(double ang) {
        ang = (ang > 180) ? ang - 360 : ang;
        ang = (ang < -180) ? ang + 360 : ang;
        return ang;
    }

    public static Vec2D playerToAudience(Vec2D playerVector) {
        if (mySide == Side.GoalToGuardAtLeft) {
            return new Vec2D(playerVector.y, -playerVector.x);
        } else {
            return new Vec2D(-playerVector.y, playerVector.x);
        }
    }

    public static int[] playerToAudience(int[] playerVector) {
        if (mySide == Side.GoalToGuardAtLeft) {
            return new int[]{playerVector[1], -playerVector[0]};
        } else {
            return new int[]{-playerVector[1], playerVector[0]};
        }
    }

    public static double playerToAudience(double playerAngle) {
        if (mySide == Side.GoalToGuardAtLeft) {
            return playerAngle;
        } else {
            return normAng(playerAngle - 180);
        }
    }

    public static double calcAngDiff(double angA, double angB) {
        return normAng(angA - angB);
    }
}
