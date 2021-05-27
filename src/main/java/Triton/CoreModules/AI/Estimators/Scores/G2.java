package Triton.CoreModules.AI.Estimators.Scores;

import Triton.CoreModules.AI.Estimators.ProbFinder;
import Triton.CoreModules.AI.Estimators.Score;
import Triton.Misc.Math.Matrix.Vec2D;

import static Triton.Config.OldConfigs.GeometryConfig.FIELD_LENGTH;
import static Triton.Config.OldConfigs.GeometryConfig.GOAL_LENGTH;

/**
 * g2: There is a wide enough open angle θ from x to the opposing goal
 */
public class G2 extends Score {

    private static final double G2_MEAN = 20.0;
    private static final double G2_DEV = 40.0;

    public G2(ProbFinder finder) {
        super(finder);
    }

    @Override
    public double prob(Vec2D pos) {
        Vec2D leftGoal = new Vec2D(-GOAL_LENGTH / 2, FIELD_LENGTH / 2);
        Vec2D rightGoal = leftGoal.add(GOAL_LENGTH, 0);
        double openAngle = angDiff(rightGoal.sub(pos).toPlayerAngle(), leftGoal.sub(pos).toPlayerAngle());
        return (openAngle - G2_MEAN) / G2_DEV;
    }
}
