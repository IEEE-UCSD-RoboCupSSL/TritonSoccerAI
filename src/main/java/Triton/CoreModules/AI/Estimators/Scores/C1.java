package Triton.CoreModules.AI.Estimators.Scores;

import Triton.CoreModules.AI.Estimators.ProbMapModule;
import Triton.CoreModules.AI.Estimators.Score;
import Triton.CoreModules.Robot.RobotSnapshot;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

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
        for (Triton.CoreModules.Robot.RobotSnapshot foeSnap : foeSnaps) {
            // Find closest foe
            double ETA = calcETA(foeSnap, pos, fast);
            foeTime = Math.min(ETA, foeTime);
        }
        return foeTime - receiverTime;
    }
}
