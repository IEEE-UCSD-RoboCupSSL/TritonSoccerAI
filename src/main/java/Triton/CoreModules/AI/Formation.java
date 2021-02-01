package Triton.CoreModules.AI;

import Triton.Config.ObjectConfig;
import Triton.CoreModules.Robot.Ally;
import Triton.Misc.Coordinates.Vec2D;

import java.util.ArrayList;

public class Formation {

    private static Formation formation = null;

    private Formation() {

    }

    public static Formation getInstance() {
        if (formation == null) {
            formation = new Formation();
        }
        return formation;
    }

    public static boolean xxxFormation(/*...*/) {
        return false;
    }

    public boolean defaultFormation(ArrayList<Ally> bots) {
        ArrayList<Vec2D> formationPoints = new ArrayList<Vec2D>(ObjectConfig.ROBOT_COUNT - 1);
        formationPoints.add(new Vec2D(0.00, -1000));
        formationPoints.add(new Vec2D(-1000.00, -2000));
        formationPoints.add(new Vec2D(1000.00, -2000));
        formationPoints.add(new Vec2D(-2000.00, -3000));
        formationPoints.add(new Vec2D(2000.00, -3000));

        ArrayList<Double> formationAngle = new ArrayList<Double>(ObjectConfig.ROBOT_COUNT - 1);
        formationAngle.add(0.0);
        formationAngle.add(0.0);
        formationAngle.add(0.0);
        formationAngle.add(0.0);
        formationAngle.add(0.0);

        return moveToFormation(formationPoints, formationAngle, bots);
    }

    public boolean moveToFormation(ArrayList<Vec2D> formationPoints, ArrayList<Double> formationAngle, ArrayList<Ally> bots) {
        for (Ally ally : bots) {
            int botID = ally.getID();
            Vec2D targetPos = formationPoints.get(botID);
            double targetAngle = formationAngle.get(botID);
            ally.sprintToAngle(targetPos, targetAngle);
        }

        // return false when any robot is outside of their designated formation point
        for (Ally ally : bots) {
            int botID = ally.getID();
            Vec2D targetPos = formationPoints.get(botID);
            double targetAngle = formationAngle.get(botID);
            if (!ally.isPosArrived(targetPos) || !ally.isDirAimed(targetAngle))
                return false;
        }

        // return true when all our robots have arrived at their designated formation points
        return true;
    }

    public boolean testerFormation(ArrayList<Ally> bots) {
        ArrayList<Vec2D> formationPoints = new ArrayList<Vec2D>(ObjectConfig.ROBOT_COUNT - 1);
        formationPoints.add(new Vec2D(0.00, -3000));
        formationPoints.add(new Vec2D(-1000.00, -3000));
        formationPoints.add(new Vec2D(1000.00, -3000));
        formationPoints.add(new Vec2D(-2000.00, -3000));
        formationPoints.add(new Vec2D(2000.00, -3000));

        ArrayList<Double> formationAngle = new ArrayList<Double>(ObjectConfig.ROBOT_COUNT - 1);
        formationAngle.add(0.0);
        formationAngle.add(0.0);
        formationAngle.add(0.0);
        formationAngle.add(0.0);
        formationAngle.add(0.0);

        return moveToFormation(formationPoints, formationAngle, bots);
    }

    public boolean freeKickFormation(/*...*/) {
        return false;
    }

    public boolean penaltyFormation(/*...*/) {
        return false;
    }
}
