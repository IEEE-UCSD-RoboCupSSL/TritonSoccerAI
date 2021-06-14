package Triton.CoreModules.AI.Estimators.Scores;

import Triton.CoreModules.AI.Estimators.ProbFinder;
import Triton.CoreModules.AI.Estimators.Score;
import Triton.CoreModules.AI.Estimators.TimeEstimator.BallMovement;
import Triton.Misc.Math.Matrix.Vec2D;

import static Triton.Config.GlobalVariblesAndConstants.GvcGeometry.*;
import static Triton.Config.GlobalVariblesAndConstants.GvcGeometry.GOAL_LENGTH;

/**
 * g1: Shots from x can reach the opposing goal faster than their goalkeeper can block them.
 */
public class G1 extends Score {

    private static final int G1_GOAL_INTERVAL = 3;
    private static final int G1_INTERCEPT_INTERVAL = 3;

    public G1(ProbFinder finder) {
        super(finder);
    }

    @Override
    public double prob(Vec2D pos) {
        Vec2D leftGoal = new Vec2D(-GOAL_LENGTH / 2, FIELD_LENGTH / 2);
        Vec2D goalSeg = new Vec2D(GOAL_LENGTH, 0).scale(1.0 / G1_GOAL_INTERVAL);
        double g1 = Double.MAX_VALUE;

        for (int i = 1; i < G1_GOAL_INTERVAL; i++) {
            double g1_ = Double.MAX_VALUE;
            Vec2D goal = leftGoal.add(goalSeg.scale(i));
            Vec2D xToGoal = goal.sub(pos).scale(1.0 / G1_INTERCEPT_INTERVAL);
            for (int j = 1; j < G1_INTERCEPT_INTERVAL; j++) {
                Vec2D path = xToGoal.scale(j);
                Vec2D interceptPos = pos.add(path);
                double ballTime = BallMovement.calcETAFast(SHOOT_VEL, path.mag(), shootMaxPair);
                double foeTime = Double.MAX_VALUE;
                for (Triton.CoreModules.Robot.RobotSnapshot foeSnap : foeSnaps) {
                    Vec2D foePos = foeSnap.getPos();
                    double[] angleRange = angleRange(foePos, pos);
                    if (foePos.sub(pos).mag() - FRONT_PADDING < goal.sub(pos).mag() &&
                            angleBetween(path.toPlayerAngle(), angleRange)) {
                        foeTime = 0;
                        continue;
                    }
                    double ETA = calcETA(foeSnap, interceptPos);
                    foeTime = Math.min(ETA, foeTime);
                }
                g1_ = Math.min(foeTime - ballTime, g1_);
            }
            g1 = Math.min(g1_, g1);
        }
        return g1;
    }

}
