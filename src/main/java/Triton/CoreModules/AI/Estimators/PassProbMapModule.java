package Triton.CoreModules.AI.Estimators;

import Triton.CoreModules.AI.Estimators.Scores.*;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.RWLockee;
import Triton.SoccerObjects;

import java.util.HashMap;

public class PassProbMapModule extends ProbMapModule {

    private static final double C1_WEIGHT = 1.0;
    private static final double C2_WEIGHT = 1.0;
    private static final double C3_WEIGHT = 1.5;
    private static final double C4_WEIGHT = 0.5;
    private static final double C5_WEIGHT = 2.0;
    private static final double G1_WEIGHT = 2.0;
    private static final double G2_WEIGHT = 1.5;
    private static final double G3_WEIGHT = 2.0;

    private volatile String score = "all";
    private volatile Integer passer = null;

    protected RWLockee<double[][]> gWrapper = new RWLockee<>(null);
    protected RWLockee<int[][]> rWrapper = new RWLockee<>(null);

    public PassProbMapModule(SoccerObjects soccerObjects) {
        this(soccerObjects.fielders, soccerObjects.foes, soccerObjects.ball);
    }

    public PassProbMapModule(SoccerObjects soccerObjects, int resolutionStepSize, int evalWindowSize) {
        this(soccerObjects.fielders, soccerObjects.foes, soccerObjects.ball, resolutionStepSize, evalWindowSize);
    }

    public PassProbMapModule(RobotList<Ally> fielders, RobotList<Foe> foes, Ball ball) {
        this(fielders, foes, ball, 100, 20);
    }


    public PassProbMapModule(RobotList<Ally> fielders, RobotList<Foe> foes, Ball ball,
                             int resolutionStepSize, int evalWindowSize) {
        super(fielders, foes, ball, resolutionStepSize, evalWindowSize);
    }


    public int[][] getR() {
        update();
        return rWrapper.get();
    }


    public double[][] getGProb() {
        update();
        return gWrapper.get();
    }


    @Override
    protected void calcProb() {
        double[][] pmf = new double[width][height];
        double[][] gProbs = new double[width][height];
        int[][] R = new int[width][height];
        Vec2D[][] localMaxPos = new Vec2D[evalWidth][evalHeight];
        double[][] localMax = new double[evalWidth][evalHeight];
        double[][] localMaxScore = new double[evalWidth][evalHeight];

        Vec2D ballPos = ballPosWrapper.get();

        for(int i = 0; i < evalWidth; i++) {
            for(int j = 0; j < evalHeight; j++) {
                localMax[i][j] = 0.0;
            }
        }

        /* Find passer (default as closest fielder) **/
        double minDist = Double.MAX_VALUE;
        passer = 0;
        for(int i = 0; i < fielders.size(); i++) {
            double temp;
            if ((temp = fielderSnaps.get(i).getPos().sub(ballPos).mag()) < minDist) {
                minDist = temp;
                passer = i;
            }
        }

        HashMap<Integer, Score> c1 = new HashMap<>();
        Score c2 = new C2(this);
        Score c3 = new C3(this);
        Score c4 = new C4(this);
        Score c5 = new C5(this, allyPenaltyRegion, foePenaltyRegion);

        Score g1 = new G1(this);
        Score g2 = new G2(this);
        HashMap<Integer, Score> g3 = new HashMap<>();

        for (int cand = 0; cand < fielders.size(); cand++) {
            if (cand == passer) continue; // skip passer as possible candidate
            c1.put(cand, new C1(this, cand));
            g3.put(cand, new G3(this, cand));
        }

        for (int gridX = gridOrigin[0]; gridX < width; gridX++) {
            for (int gridY = gridOrigin[1]; gridY < height; gridY++) {

                Vec2D pos = grid.fromInd(gridX, gridY);

                double c2prob = c2.prob(pos);
                double c3prob = c3.prob(pos);
                double c4prob = c4.prob(pos);
                double c5prob = c5.prob(pos);

                double g1prob = g1.prob(pos);
                double g2prob = g2.prob(pos);

                if (c4prob == - Double.MAX_VALUE || c5prob == - Double.MAX_VALUE) continue;

                double maxProb = 0.0;
                double maxGProb = 0.0;
                int receiver = 0;

                for (int cand = 0; cand < fielders.size(); cand++) {
                    /* Skip passer **/
                    if (cand == passer) continue;

                    double c1prob = c1.get(cand).prob(pos);
                    double g3prob = g3.get(cand).prob(pos);

                    double c = c1prob * C1_WEIGHT + c2prob * C2_WEIGHT + c3prob * C3_WEIGHT
                            + c4prob * C4_WEIGHT + c5prob * C5_WEIGHT;
                    double g = g1prob * G1_WEIGHT + g2prob * G2_WEIGHT + g3prob * G3_WEIGHT;

                    double score = switch (this.score) {
                        case "all" -> c + g;
                        case "c1"  -> c1prob;
                        case "c2"  -> c2prob;
                        case "c3"  -> c3prob;
                        case "c4"  -> c4prob;
                        case "c5"  -> c5prob;
                        case "g1"  -> g1prob;
                        case "g2"  -> g2prob;
                        case "g3"  -> g3prob;
                        default    -> c + g;
                    };

                    double prob = (1 / (1 + Math.exp(-score)));
                    double gProb = (1 / (1 + Math.exp(-g)));

                    if (prob > maxProb) {
                        maxProb = prob;
                        receiver = cand;
                    }

                    if (gProb > maxGProb) {
                        maxGProb = gProb;
                    }
                }

                pmf[gridX][gridY] = maxProb;
                gProbs[gridX][gridY] = maxGProb;
                R[gridX][gridY] = receiver;
                int[] evalIdx = evalGrid.fromPos(new Vec2D(gridX, gridY));
                if(maxProb > localMax[evalIdx[0]][evalIdx[1]]) {
                    localMax[evalIdx[0]][evalIdx[1]] = maxProb;
                    localMaxPos[evalIdx[0]][evalIdx[1]] = pos;
                }
                localMaxScore[evalIdx[0]][evalIdx[1]] += maxProb;
            }
        }

        rWrapper.set(R);
        gWrapper.set(gProbs);
        pmfWrapper.set(pmf);
        localMaxPosWrapper.set(localMaxPos);
        localMaxScoreWrapper.set(localMax);
    }

    public void fixScore(String score) {
        this.score = score;
    }

    /**
     * @return a tuple of information needed by pass
     */
    public PassInfo evalPass() {
        update();
        try {
            double[][] pmf = pmfWrapper.get();
            if (pmf == null) return null;
            int[][] receiver = rWrapper.get();
            if (receiver == null) return null;

            Vec2D topPos = getTopNMaxPos(1).get(0);
            int[] idx = getIdxFromPos(topPos);
            double maxProb = pmf[idx[0]][idx[1]];
            int bestReceiver = receiver[idx[0]][idx[1]];

            PassInfo info = new PassInfo(fielders, foes, ball);
            info.setInfo(passer, bestReceiver, fielderSnaps.get(passer).getPos(), topPos, maxProb);
            return info;
        } catch (Exception e) {
            return null;
        }
    }
}
