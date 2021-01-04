package Triton.Dependencies;

import Triton.Config.ObjectConfig;
import Triton.Dependencies.Shape.Vec2D;

public class PerspectiveConverter {
    public static Vec2D audienceToPlayer(Vec2D audienceVector) {
        if (ObjectConfig.MY_TEAM == Team.BLUE) {
            return new Vec2D(-audienceVector.y, audienceVector.x);
        } else {
            return new Vec2D(audienceVector.y, -audienceVector.x);
        }
    }

    public static int[] audienceToPlayer(int[] audienceVector) {
        if (ObjectConfig.MY_TEAM == Team.BLUE) {
            return new int[]{-audienceVector[1], audienceVector[0]};
        } else {
            return new int[]{audienceVector[1], -audienceVector[0]};
        }
    }

    public static double audienceToPlayer(double audienceAngle) {
        if (ObjectConfig.MY_TEAM == Team.BLUE) {
            return audienceAngle;
        } else {
            return audienceAngle + 180;
        }
    }
    
    public static Vec2D playerToAudience(Vec2D playerVector) {
        if (ObjectConfig.MY_TEAM == Team.BLUE) {
            return new Vec2D(playerVector.y, -playerVector.x);
        } else {
            return new Vec2D(-playerVector.y, playerVector.x);
        }
    }

    public static int[] playerToAudience(int[] playerVector) {
        if (ObjectConfig.MY_TEAM == Team.BLUE) {
            return new int[]{playerVector[1], -playerVector[0]};
        } else {
            return new int[]{-playerVector[1], playerVector[0]};
        }
    }

    public static double playerToAudience(double playerAngle) {
        if (ObjectConfig.MY_TEAM == Team.BLUE) {
            return playerAngle;
        } else {
            return playerAngle - 180;
        }
    }
}
