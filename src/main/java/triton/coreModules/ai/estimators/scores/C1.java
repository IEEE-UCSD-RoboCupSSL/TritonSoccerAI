package triton.coreModules.ai.estimators.scores;

import triton.coreModules.ai.estimators.ProbMapModule;
import triton.coreModules.ai.estimators.Score;
import triton.coreModules.robot.RobotSnapshot;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.ArrayList;

/**
 * c1: No opponent can reach pos faster than receiver can.
 */
public class C1 extends Score {

    private final int candidate;
    private final boolean fast;

    public C1(ProbMapModule finder, int candidate, boolean fast) {
        super(finder);
        this.candidate = candidate;
        this.fast = fast;
    }

    public C1(Vec2D ballPos, ArrayList<RobotSnapshot> fielderSnaps,
              ArrayList<RobotSnapshot> foeSnaps, int candidate, boolean fast) {
        super(ballPos, fielderSnaps, foeSnaps);
        this.candidate = candidate;
        this.fast = fast;
    }

    @Override
    public double prob(Vec2D pos) {
        double receiverTime = calcETA(fielderSnaps.get(candidate), pos, fast);
        double foeTime = Double.MAX_VALUE;
        for (triton.coreModules.robot.RobotSnapshot foeSnap : foeSnaps) {
            // Find closest foe
            double ETA = calcETA(foeSnap, pos, fast);
            foeTime = Math.min(ETA, foeTime);
        }
        return foeTime - receiverTime;
    }
}
