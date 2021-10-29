package triton.coreModules.ai.estimators.scores;

import triton.coreModules.ai.estimators.ProbMapModule;
import triton.coreModules.ai.estimators.Score;
import triton.coreModules.ai.estimators.timeEstimator.BallMovement;
import triton.coreModules.robot.RobotSnapshot;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.ArrayList;

/**
 * c2 : No opponent intercepts the pass.
 */
public class C2 extends Score {

    private static final int C2_INTERVAL = 5;
    private static final double CHIP_KICK_DIST = 500.0;
    private static final double CHIP_KICK_DECAY = 500.0;
    private final boolean fast;

    public C2(ProbMapModule finder, boolean fast) {
        super(finder);
        this.fast = fast;
    }

    public C2(Vec2D ballPos, ArrayList<RobotSnapshot> fielderSnaps,
              ArrayList<RobotSnapshot> foeSnaps, boolean fast) {
        super(ballPos, fielderSnaps, foeSnaps);
        this.fast = fast;
    }

    @Override
    public double prob(Vec2D pos) {
        double c2 = Double.MAX_VALUE;

        Vec2D ballToPos = pos.sub(ballPos).scale(1.0 / C2_INTERVAL);
        for (int i = 1; i < C2_INTERVAL; i++) {
            Vec2D path = ballToPos.scale(i);
            Vec2D interceptPos = ballPos.add(path);
            double ballTime = BallMovement.calcETAFast(PASS_VEL, path.mag(), passMaxPair);
            double foeTime = Double.MAX_VALUE;
            for (triton.coreModules.robot.RobotSnapshot foeSnap : foeSnaps) {
                Vec2D foePos = foeSnap.getPos();
                double[] angleRange = angleRange(foePos, ballPos);
                double ETA = calcETA(foeSnap, interceptPos, fast);
                if (foePos.sub(ballPos).mag() - FRONT_PADDING < pos.sub(ballPos).mag() &&
                        angleBetween(path.toPlayerAngle(), angleRange)) {
                    double chipDist = pos.sub(ballPos).mag() - foePos.sub(ballPos).mag();
                    chipDist = Math.max(chipDist - CHIP_KICK_DIST, 0);
                    foeTime = chipDist / CHIP_KICK_DECAY;
                    foeTime = Math.min(ETA, foeTime);
                } else {
                    foeTime = Math.min(ETA, foeTime);
                }
            }
            c2 = Math.min(foeTime - ballTime, c2);
        }

        return c2;
    }
}
