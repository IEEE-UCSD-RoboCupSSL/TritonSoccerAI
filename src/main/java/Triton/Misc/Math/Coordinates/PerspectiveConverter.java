package Triton.Misc.Math.Coordinates;

import Triton.Config.OldConfigs.ObjectConfig;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.VirtualBot.VirtualBotCmds;

public class PerspectiveConverter {
    private static Team myTeam = Team.BLUE; // default 
    public static void setTeam(Team team) {
        PerspectiveConverter.myTeam = team;
    }
    
    public static Vec2D audienceToPlayer(Vec2D audienceVector) {
        if (myTeam == Team.BLUE) {
            return new Vec2D(-audienceVector.y, audienceVector.x);
        } else {
            return new Vec2D(audienceVector.y, -audienceVector.x);
        }
    }

    public static int[] audienceToPlayer(int[] audienceVector) {
        if (myTeam == Team.BLUE) {
            return new int[]{-audienceVector[1], audienceVector[0]};
        } else {
            return new int[]{audienceVector[1], -audienceVector[0]};
        }
    }

    public static double audienceToPlayer(double audienceAngle) {
        if (myTeam == Team.BLUE) {
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
        if (myTeam == Team.BLUE) {
            return new Vec2D(playerVector.y, -playerVector.x);
        } else {
            return new Vec2D(-playerVector.y, playerVector.x);
        }
    }

    public static int[] playerToAudience(int[] playerVector) {
        if (myTeam == Team.BLUE) {
            return new int[]{playerVector[1], -playerVector[0]};
        } else {
            return new int[]{-playerVector[1], playerVector[0]};
        }
    }

    public static double playerToAudience(double playerAngle) {
        if (myTeam == Team.BLUE) {
            return playerAngle;
        } else {
            return normAng(playerAngle - 180);
        }
    }

    public static double calcAngDiff(double angA, double angB) {
        return normAng(angA - angB);
    }
}
