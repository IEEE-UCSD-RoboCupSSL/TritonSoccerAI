package triton.coreModules.ai.estimators.scores;

import triton.coreModules.ai.estimators.ProbMapModule;
import triton.coreModules.ai.estimators.Score;
import triton.coreModules.robot.RobotSnapshot;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.ArrayList;

import static triton.config.globalVariblesAndConstants.GvcGeometry.FIELD_LENGTH;
import static triton.config.globalVariblesAndConstants.GvcGeometry.GOAL_LENGTH;

/**
 * g2: There is a wide enough open angle θ from x to the opposing goal
 */
public class G2 extends Score {

    private static final double G2_MEAN = 0.0;
    private static final double G2_DEV = 40.0;

    public G2(ProbMapModule finder) {
        super(finder);
    }

    public G2(Vec2D ballPos, ArrayList<RobotSnapshot> fielderSnaps,
              ArrayList<RobotSnapshot> foeSnaps) {
        super(ballPos, fielderSnaps, foeSnaps);
    }

    @Override
    public double prob(Vec2D pos) {
        Vec2D leftGoal = new Vec2D(-GOAL_LENGTH / 2, FIELD_LENGTH / 2);
        Vec2D rightGoal = leftGoal.add(GOAL_LENGTH, 0);
        double a1 = rightGoal.sub(pos).toPlayerAngle();
        double a2 = leftGoal.sub(pos).toPlayerAngle();
        if (a1 == -90.0) {
            a1 = 0.0;
        }
        if (a2 == -90.0) {
            a2 = 0.0;
        }
        double openAngle = angDiff(a1, a2);
        return (openAngle - G2_MEAN) / G2_DEV;
    }
}
