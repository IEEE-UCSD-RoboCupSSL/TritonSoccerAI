package Triton.CoreModules.AI.Estimators.Scores;

import Triton.CoreModules.AI.Estimators.ProbMapModule;
import Triton.CoreModules.AI.Estimators.Score;
import Triton.CoreModules.AI.Estimators.TimeEstimator.BallMovement;
import Triton.CoreModules.Robot.RobotSnapshot;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

import java.util.ArrayList;

/**
 * c2 : No opponent intercepts the pass.
 */
public class C2 extends Score {

    private static final int C2_INTERVAL = 5;
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
            for (Triton.CoreModules.Robot.RobotSnapshot foeSnap : foeSnaps) {
                Vec2D foePos = foeSnap.getPos();
                double[] angleRange = angleRange(foePos, ballPos);
                if (foePos.sub(ballPos).mag() - FRONT_PADDING < pos.sub(ballPos).mag() &&
                        angleBetween(path.toPlayerAngle(), angleRange)) {
                    foeTime = 0;
                    continue;
                }
                double ETA = calcETA(foeSnap, interceptPos, fast);
                foeTime = Math.min(ETA, foeTime);
            }
            c2 = Math.min(foeTime - ballTime, c2);
        }

        return c2;
    }
}
