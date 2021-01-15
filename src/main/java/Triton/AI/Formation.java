package Triton.AI;

import Triton.Config.ObjectConfig;
import Triton.Dependencies.Shape.Vec2D;
import Triton.Objects.Ally;
import Triton.Objects.Robot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class Formation {

    private static Formation formation = null;

    private Formation() {

    }

    public static Formation getInstance() {
        if(formation == null) {
            formation = new Formation();
        }
        return formation;
    }

    public static boolean defaultFormation(Ally[] bots) {
        return defaultFormation((ArrayList<Ally>)Arrays.asList(bots));
    }
    public static boolean defaultFormation(ArrayList<Ally> bots) {
        Vec2D[] formationPoints = new Vec2D[ObjectConfig.ROBOT_COUNT - 1];
        formationPoints[0] = new Vec2D(0.00, -1000);
        formationPoints[1] = new Vec2D(-1000.00, -2000);
        formationPoints[2] = new Vec2D(1000.00, -2000);
        formationPoints[3] = new Vec2D(-2000.00, -3000);
        formationPoints[4] = new Vec2D(2000.00, -3000);

        for(Ally bot : bots) {
            // ...
        }

        // return true when all our robots have arrived at their designated formation points
        return false;
    }

    public static boolean freeKickFormation(/*...*/) {
        return false;
    }


    public static boolean penaltyFormation(/*...*/) {
        return false;
    }

    public static boolean xxxFormation(/*...*/) {
        return false;
    }



}
