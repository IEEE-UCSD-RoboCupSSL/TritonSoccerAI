package Triton.CoreModules.AI.Estimators.Scores;

import Triton.CoreModules.AI.Estimators.ProbMapModule;
import Triton.CoreModules.AI.Estimators.Score;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

public class C1 extends Score {

    private final int candidate;

    public C1(ProbMapModule finder, int candidate) {
        super(finder);
        this.candidate = candidate;
    }

    @Override
    public double prob(Vec2D pos) {
        double receiverTime = calcETA(fielderSnaps.get(candidate), pos);
        double foeTime = Double.MAX_VALUE;
        for (Triton.CoreModules.Robot.RobotSnapshot foeSnap : foeSnaps) {
            // Find closest foe
            double ETA = calcETA(foeSnap, pos);
            foeTime = Math.min(ETA, foeTime);
        }
        return foeTime - receiverTime;
    }
}
