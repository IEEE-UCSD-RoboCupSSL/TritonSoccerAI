package Triton.CoreModules.AI.TritonProbDijkstra.ComputableImpl;

import Triton.CoreModules.AI.Estimators.PassInfo;
import Triton.CoreModules.AI.Estimators.ProbMapModule;
import Triton.CoreModules.AI.Estimators.Score;
import Triton.CoreModules.AI.Estimators.Scores.*;
import Triton.CoreModules.AI.TritonProbDijkstra.Computables.DijkCompute;
import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;
import Triton.CoreModules.Robot.RobotSnapshot;
import Triton.Misc.Math.Geometry.Rect2D;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.RWLockee;
import java.util.ArrayList;

import static Triton.Config.GlobalVariblesAndConstants.GvcGeometry.TOP_TOUCH_LINE;
import static Triton.Config.GlobalVariblesAndConstants.GvcGeometry.BOTTOM_TOUCH_LINE;
import static Triton.Misc.Math.Coordinates.PerspectiveConverter.audienceToPlayer;

import lombok.Getter;
import lombok.Setter;
import org.javatuples.Pair;

@Getter
@Setter
public class Compute implements DijkCompute {

    private static final double C1_WEIGHT = 1.0;
    private static final double C2_WEIGHT = 1.0;
    private static final double C3_WEIGHT = 1.0;
    private static final double C4_WEIGHT = 1.0;
    private static final double C5_WEIGHT = 1.0;

    private static final double G1_WEIGHT = 1.0;
    private static final double G2_WEIGHT = 1.0;

    private static final double SAMPLE_PADDING  = 250.0;
    private static final double SAMPLE_INTERVAL = 50.0;

    private ArrayList<RobotSnapshot> fielderSnaps;
    private ArrayList<RobotSnapshot> foeSnaps;
    private Vec2D ballPos;
    private PassInfo info = null;
    private Rect2D allyPenaltyRegion, foePenaltyRegion;

    public Compute() {
        Rect2D[] penaltyRegions = ProbMapModule.getPenaltyRegions();
        allyPenaltyRegion = penaltyRegions[0];
        foePenaltyRegion = penaltyRegions[1];
    }

    public void computePass(PUAG.Node n1, PUAG.Node n2) {
        /* Return if have computed for current data */
        if (info != null) return;

        assert n1.getBot() != null && n2.getBot() != null;

        /* Initiate scores */
        Score c1 = new C1(ballPos, fielderSnaps, foeSnaps, n2.getBot().getID(), true);
        Score c2 = new C2(ballPos, fielderSnaps, foeSnaps, true);
        Score c3 = new C3(ballPos, fielderSnaps, foeSnaps);
        Score c4 = new C4(ballPos, fielderSnaps, foeSnaps);
        Score c5 = new C5(ballPos, fielderSnaps, foeSnaps, allyPenaltyRegion, foePenaltyRegion);

        /* Evaluate reception points */
        double[] bnds = getMinMax(n1.getBot().getPos(), n2.getBot().getPos());
        Vec2D corner1 = audienceToPlayer(TOP_TOUCH_LINE.p2);
        Vec2D corner2 = audienceToPlayer(BOTTOM_TOUCH_LINE.p1);
        double[] fields = getMinMax(corner1, corner2);

        Vec2D receivingPos = null;
        double prob = -Double.MAX_VALUE;

        for (double x = bnds[0] - SAMPLE_PADDING; x < bnds[1] + SAMPLE_PADDING; x += SAMPLE_INTERVAL) {
            for (double y = bnds[2] - SAMPLE_PADDING; y < bnds[3] + SAMPLE_PADDING; y += SAMPLE_INTERVAL) {
                if (x < fields[0] || x > fields[1] || y < fields[2] || y > fields[3]) {
                    continue; // out of field
                }
                Vec2D pos = new Vec2D(x, y);

                double c1prob = c1.prob(pos);
                double c2prob = c2.prob(pos);
                double c3prob = c3.prob(pos);
                double c4prob = c4.prob(pos);
                double c5prob = c5.prob(pos);

                double c = c1prob * C1_WEIGHT + c2prob * C2_WEIGHT + c3prob * C3_WEIGHT
                        + c4prob * C4_WEIGHT + c5prob * C5_WEIGHT;
                double cprob = (1 / (1 + Math.exp(-c)));
                if (cprob > prob) {
                    prob = cprob;
                    receivingPos = pos;
                }
            }
        }

        info = new PassInfo();
        info.setInfo(n1.getBot(), n2.getBot(), n1.getBot().getPos(), receivingPos, prob);
    }

    private static double[] getMinMax(Vec2D v1, Vec2D v2) {
        double xmin, xmax, ymin, ymax;
        xmin = v1.x;
        xmax = v2.x;
        if (xmax < xmin) {
            double temp = xmin;
            xmin = xmax;
            xmax = temp;
        }
        ymin = v1.y;
        ymax = v2.y;
        if (ymax < ymin) {
            double temp = ymin;
            ymin = xmax;
            ymax = temp;
        }
        return new double[]{xmin, xmax, ymin, ymax};
    }

    @Override
    public double computeAngle(PUAG.Node n1, PUAG.Node n2) {
        assert n1.getBot() != null && n2.getBot() != null;

        Vec2D path = n2.getBot().getPos().sub(n1.getBot().getPos());
        return path.toPlayerAngle();
    }

    @Override
    public Vec2D computeGoalCenter(PUAG.Node n) {
        return foePenaltyRegion.anchor.add(new Vec2D(foePenaltyRegion.width / 2.0, foePenaltyRegion.height));
    }

    @Override
    public void setSnapShots(ArrayList<RobotSnapshot> allySnaps, ArrayList<RobotSnapshot> foeSnaps,
                             RWLockee<Vec2D> ballSnap) {
        this.fielderSnaps = allySnaps;
        this.foeSnaps = foeSnaps;
        this.ballPos = ballSnap.get();
        info = null;
    }

    @Override
    public Vec2D computeKickVec(PUAG.Node n1, PUAG.Node n2) {
        computePass(n1, n2);
        info.setRobots(fielderSnaps, foeSnaps);
        Pair<Vec2D, Boolean> kick = info.getKickDecision();
        return kick.getValue0();
    }

    @Override
    public Vec2D computePasspoint(PUAG.Node n1, PUAG.Node n2) {
        assert n1.getBot() != null && n2.getBot() != null;
        return n1.getBot().getPos();
    }

    @Override
    public double computeProb(PUAG.Node n1, PUAG.Node n2) {
        computePass(n1, n2);
        return info.getMaxProb();
    }

    @Override
    public double computeGoalProb(PUAG.Node n) {
        assert n.getBot() != null;
        Vec2D pos = n.getBot().getPos();

        /* Initiate scores */
        Score g1 = new G1(ballPos, fielderSnaps, foeSnaps, true);
        Score g2 = new G2(ballPos, fielderSnaps, foeSnaps);

        /* Evaluate goal probability */
        double g1prob = g1.prob(pos);
        double g2prob = g2.prob(pos);
        double g = g1prob * G1_WEIGHT + g2prob * G2_WEIGHT;

        return (1 / (1 + Math.exp(-g)));
    }

    @Override
    public Vec2D computeRecepPoint(PUAG.Node n1, PUAG.Node n2) {
        computePass(n1, n2);
        return info.getOptimalReceivingPos();
    }
}
