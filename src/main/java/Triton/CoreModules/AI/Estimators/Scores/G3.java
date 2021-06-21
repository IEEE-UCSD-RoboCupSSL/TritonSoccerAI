package Triton.CoreModules.AI.Estimators.Scores;

import Triton.CoreModules.AI.Estimators.ProbMapModule;
import Triton.CoreModules.AI.Estimators.Score;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

import static Triton.Config.GlobalVariblesAndConstants.GvcGeometry.FIELD_LENGTH;
import static Triton.Config.GlobalVariblesAndConstants.GvcGeometry.GOAL_LENGTH;

/**
 * g3: R will have enough time to take a shot before opponents steal the ball
 */
public class G3 extends Score {

    private static final int G3_GOAL_INTERVAL = 5;
    private static final double G3_ONE_SHOT_ANGLE = 20;

    private final int candidate;

    public G3(ProbMapModule finder, int candidate) {
        super(finder);
        this.candidate = candidate;
    }

    @Override
    public double prob(Vec2D pos) {
        Vec2D leftGoal = new Vec2D(-GOAL_LENGTH / 2, FIELD_LENGTH / 2);
        Vec2D goalSeg = new Vec2D(GOAL_LENGTH, 0).scale(1.0 / G3_GOAL_INTERVAL);

        double g3 = 0.0;
        Vec2D rPos = fielderSnaps.get(candidate).getPos();
        for (int i = 0; i <= G3_GOAL_INTERVAL; i++) {
            Vec2D goal = leftGoal.add(goalSeg.scale(i));
            if (goal.sub(rPos).mag() > goal.sub(pos).mag()) {
                g3 += 1.0;
            }
            if (angDiff(goal.sub(pos).toPlayerAngle(),
                    pos.sub(rPos).toPlayerAngle()) < G3_ONE_SHOT_ANGLE) {
                g3 += 1.0;
            }
        }
        return g3;
    }
}
