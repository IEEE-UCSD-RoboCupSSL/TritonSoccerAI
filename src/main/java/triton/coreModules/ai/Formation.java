package triton.coreModules.ai;

import triton.config.globalVariblesAndConstants.GvcAI;
import triton.coreModules.ai.skills.Swarm;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.RobotList;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class Formation {

   static public void printAvailableFormations(){

        System.out.println("Available Formations:");
        for (String s : preset.keySet()) {
            System.out.printf("- %s \n", s);
        }
    }

    public static final Map<String, FormationType> preset = Map.of(
            "default", new FormationType(
                    new ArrayList<>(Arrays.asList(
                            new Vec2D(-2000.00, -3000.00),
                            new Vec2D(-1000.00, -2000.00),
                            new Vec2D(0.00, -1000.00),
                            new Vec2D(1000.00, -2000.00),
                            new Vec2D(2000.00, -3000.00)
                    )),
                    new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0))
            ),

            "tester", new FormationType(
                    new ArrayList<>(Arrays.asList(
                            new Vec2D(0.00, -3000.00),
                            new Vec2D(-1000.00, -3000.00),
                            new Vec2D(1000.00, -3000.00),
                            new Vec2D(-2000.00, -3000.00),
                            new Vec2D(2000.00, -3000.00)
                    )),
                    new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0))
            ),

//            "out", new FormationType(
//                    new ArrayList<>(Arrays.asList(
//                            new Vec2D(4450.00, -3000),
//                            new Vec2D(4450.00, -2500),
//                            new Vec2D(4450.00, -2000),
//                            new Vec2D(4450.00, -1500),
//                            new Vec2D(4450.00, -1000)
//                    )),
//                    new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0)),
//                    new Vec2D(4450.00, -3500),
//                    0.0
//            ),

            "kickoff-offense", new FormationType(
                    new ArrayList<>(Arrays.asList(
                            new Vec2D(0.00, -250.00),
                            new Vec2D(600.00, -600.00),
                            new Vec2D(1200.00, -200.00),
                            new Vec2D(-600.00, -600.00),
                            new Vec2D(-1200.00, -200.00)
                    )),
                    new ArrayList<>(Arrays.asList(0.0, 30.0, 30.0, -30.0, -30.0)),
                    new Vec2D(0.00, -4200.00),
                    0.0
            ),

            "kickoff-defense", new FormationType(
                    new ArrayList<>(Arrays.asList(
                            new Vec2D(0.00, -600.00),
                            new Vec2D(600.00, -600.00),
                            new Vec2D(1200.00, -900.00),
                            new Vec2D(-600.00, -600.00),
                            new Vec2D(-1200.00, -900.00)
                    )),
                    new ArrayList<>(Arrays.asList(0.0, 30.0, 30.0, -30.0, -30.0)),
                    new Vec2D(0.00, -4200.00),
                    0.0
            ),
            "ballplacement-defense", new FormationType(
                    new ArrayList<>(Arrays.asList(
                            new Vec2D(0.00, -2500.00),
                            new Vec2D(1000.00, -2500.00),
                            new Vec2D(-1000.00, -2500.00),
                            new Vec2D(2000.00, -3500.00),
                            new Vec2D(-2000.00, -3500.00)
                    )),
                    new ArrayList<>(Arrays.asList(0.0, 30.0, 30.0, -30.0, -30.0)),
                    new Vec2D(0.00, -4200.00),
                    0.0
            ),
            "goal-defense", new FormationType(
                    new ArrayList<>(Arrays.asList(
                            new Vec2D(0.00, -2500.00),
                            new Vec2D(1000.00, -2500.00),
                            new Vec2D(-1000.00, -2500.00),
                            new Vec2D(2000.00, -3500.00),
                            new Vec2D(-2000.00, -3500.00)
                    )),
                    new ArrayList<>(Arrays.asList(0.0, 30.0, 30.0, -30.0, -30.0)),
                    new Vec2D(0.00, -4200.00),
                    0.0
            ),
            "penalty-defense", new FormationType(
                    new ArrayList<>(Arrays.asList(
                            new Vec2D(0.00, -600.00),
                            new Vec2D(600.00, -600.00),
                            new Vec2D(1200.00, -900.00),
                            new Vec2D(-600.00, -600.00),
                            new Vec2D(-1200.00, -900.00)
                    )),
                    new ArrayList<>(Arrays.asList(0.0, 30.0, 30.0, -30.0, -30.0)),
                    new Vec2D(0.00, -3500.00),
                    0.0
            )
//            "free-kick", new FormationType(
//                    new ArrayList<>(Arrays.asList(
//                            new Vec2D(0.00, -2500.00),
//                            new Vec2D(1000.00, -2500.00),
//                            new Vec2D(-1000.00, -2500.00),
//                            new Vec2D(2000.00, -3500.00),
//                            new Vec2D(-2000.00, -3500.00)
//                    )),
//                    new ArrayList<>(Arrays.asList(0.0, 30.0, 30.0, -30.0, -30.0)),
//                    new Vec2D(4450.00, -3500.00),
//                    0.0
//            )
    );
    private static Formation formation = null;

    private Formation() {

    }

    public static Formation getInstance() {
        if (formation == null) {
            formation = new Formation();
        }
        return formation;
    }


    public boolean moveToFormation(String str, RobotList<Ally> bots) {
        return moveToFormation(preset.get(str), bots);
    }

    public boolean moveToFormation(FormationType formation, RobotList<Ally> bots) {
        if (formation.keeper) {
            System.out.println("The preset formation requires a keeper.");
            return false;
        }
        return moveToFormation(formation.points, formation.angles, bots);
    }

    public boolean moveToFormation(ArrayList<Vec2D> formationPoints, ArrayList<Double> formationAngle, RobotList<Ally> bots) {
//        for (Ally ally : bots) {
//            int botID = ally.getID();
//            Vec2D targetPos = formationPoints.get(botID);
//            double targetAngle = formationAngle.get(botID);
//            ally.curveTo(targetPos, targetAngle);
//        }
//
//        // return false when any robot is outside of their designated formation point
//        for (Ally ally : bots) {
//            int botID = ally.getID();
//            Vec2D targetPos = formationPoints.get(botID);
//            double targetAngle = formationAngle.get(botID);
//            if (!ally.isPosArrived(targetPos) || !ally.isDirAimed(targetAngle))
//                return false;
//        }
        new Swarm(bots, GvcAI.globalConfig_AdHoc).groupTo(formationPoints, formationAngle);

        // return true when all our robots have arrived at their designated formation points
        return true;
    }

    public boolean moveToFormation(String str, RobotList<Ally> bots, Ally keeper) throws NullPointerException{
        return moveToFormation(preset.get(str), bots, keeper);
    }

    public boolean moveToFormation(FormationType formation, RobotList<Ally> bots, Ally keeper) throws NullPointerException{
        if (!formation.keeper || keeper == null) {
            return moveToFormation(formation.points, formation.angles, bots);
        }
        return moveToFormation(formation.points, formation.angles, bots,
                formation.keeperPoint, formation.keeperAngle, keeper);
    }

    public boolean moveToFormation(ArrayList<Vec2D> formationPoints, ArrayList<Double> formationAngle, RobotList<Ally> bots,
                                   Vec2D keeperPoint, Double keeperAngle, Ally keeper) throws NullPointerException {

        keeper.curveTo(keeperPoint, keeperAngle);
        if (!moveToFormation(formationPoints, formationAngle, bots)) {
            return false;
        }
        //System.out.println("Keeper is moving...");
        return keeper.isPosArrived(keeperPoint) && keeper.isDirAimed(keeperAngle);
    }


    public static final class FormationType {
        public final ArrayList<Vec2D> points;
        public final ArrayList<Double> angles;
        public final boolean keeper; // whether specify keeper position; optional
        public final Vec2D keeperPoint;
        public final Double keeperAngle;

        public FormationType(ArrayList<Vec2D> points, ArrayList<Double> angles) {
            this.points = points;
            this.angles = angles;
            keeper = false;
            keeperPoint = null;
            keeperAngle = null;
        }

        public FormationType(ArrayList<Vec2D> points, ArrayList<Double> angles,
                             Vec2D keeperPoint, Double keeperAngle) {
            this.points = points;
            this.angles = angles;
            keeper = true;
            this.keeperPoint = keeperPoint;
            this.keeperAngle = keeperAngle;
        }
    }
}
