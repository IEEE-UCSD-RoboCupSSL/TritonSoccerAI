package triton.coreModules.ai.estimators.scores;

import triton.coreModules.ai.estimators.ProbMapModule;
import triton.coreModules.ai.estimators.Score;
import triton.coreModules.robot.RobotSnapshot;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.ArrayList;

/**
 * c4: The pass is short enough to be performed accurately
 */
public class C4 extends Score {

    private static final double C4_MAX_DIST = 3000.0;
    private static final double C4_MIN_DIST = 2500.0;
    private static final double C4_DEV = 500.0;

    public C4(ProbMapModule finder) {
        super(finder);
    }

    public C4(Vec2D ballPos, ArrayList<RobotSnapshot> fielderSnaps,
              ArrayList<RobotSnapshot> foeSnaps) {
        super(ballPos, fielderSnaps, foeSnaps);
    }

    @Override
    public double prob(Vec2D pos) {
        double ballToPosDist = ballPos.sub(pos).mag();
        if (ballToPosDist > C4_MAX_DIST) {
            return - Double.MAX_VALUE; // negative infinity
        }
        return Math.min(0, C4_MIN_DIST - ballToPosDist) / C4_DEV;
    }
}
