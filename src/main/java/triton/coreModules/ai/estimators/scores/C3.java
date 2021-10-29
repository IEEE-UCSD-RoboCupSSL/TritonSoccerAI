package triton.coreModules.ai.estimators.scores;
import triton.coreModules.ai.estimators.ProbMapModule;
import triton.coreModules.ai.estimators.Score;
import triton.coreModules.ai.estimators.timeEstimator.BallMovement;
import triton.coreModules.robot.RobotSnapshot;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.ArrayList;

/**
 * c3: The pass is long enough for R to react and receive the pass robustly
 */
public class C3 extends Score {

    private static final double C3_T_MIN = 0.1;
    private static final double C3_T_MAX = 0.3;
    private static final double C3_DEV = 0.1;

    public C3(ProbMapModule finder) {
        super(finder);
    }

    public C3(Vec2D ballPos, ArrayList<RobotSnapshot> fielderSnaps,
              ArrayList<RobotSnapshot> foeSnaps) {
        super(ballPos, fielderSnaps, foeSnaps);
    }

    @Override
    public double prob(Vec2D pos) {
        double ballToPosDist = ballPos.sub(pos).mag();
        double ballTime = BallMovement.calcETAFast(PASS_VEL, ballToPosDist, passMaxPair);
        double c3;

        if (ballTime < C3_T_MIN) {
            c3 = - Double.MAX_VALUE;
        } else {
            c3 = Math.min(0, ballTime - C3_T_MAX) / C3_DEV;
        }

        return c3;
    }
}
