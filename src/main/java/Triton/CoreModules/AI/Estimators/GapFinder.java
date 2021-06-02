package Triton.CoreModules.AI.Estimators;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.*;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.Misc.Math.Coordinates.Gridify;
import Triton.Misc.Math.Geometry.Line2D;
import Triton.Misc.Math.Geometry.Rect2D;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.Misc.RWLockee;
import Triton.SoccerObjects;

import java.util.ArrayList;
import java.util.ListIterator;

import static Triton.Config.OldConfigs.GeometryConfig.*;
import static Triton.Misc.Math.Coordinates.PerspectiveConverter.audienceToPlayer;
import static Triton.Misc.Math.Coordinates.PerspectiveConverter.normAng;

public class GapFinder extends ProbFinder {
    protected final RobotList<Ally> fielders;
    protected final RobotList<Foe> foes;
    protected final Ball ball;

    protected final Gridify grid;
    protected final Gridify evalGrid;
    protected int[] gridOrigin;
    protected int[] evalOrigin;
    protected int width, height;
    protected int evalWidth, evalHeight;
    int evalWindowSize;
    int resolutionStepSize;

    protected Rect2D allyPenaltyRegion, foePenaltyRegion;

    protected double responseRange = 1000.0;
    protected double interceptRange = 500.0;

    protected RWLockee<double[][]> pmfWrapper = new RWLockee<>(null);
    protected RWLockee<Vec2D[][]> localMaxPosWrapper = new RWLockee<>(null);
    protected RWLockee<double[][]> localMaxScoreWrapper = new RWLockee<>(null);

    public GapFinder(SoccerObjects soccerObjects) {
        this(soccerObjects.fielders, soccerObjects.foes, soccerObjects.ball);
    }

    public GapFinder(SoccerObjects soccerObjects, int resolutionStepSize, int evalWindowSize) {
        this(soccerObjects.fielders, soccerObjects.foes, soccerObjects.ball, resolutionStepSize, evalWindowSize);
    }

    public GapFinder(RobotList<Ally> fielders, RobotList<Foe> foes, Ball ball) {
        this(fielders, foes, ball, 100, 10);
    }


    public GapFinder(RobotList<Ally> fielders, RobotList<Foe> foes, Ball ball,
                     int resolutionStepSize, int evalWindowSize) {
        this.foes = foes;
        this.ball = ball;
        this.fielders = fielders;
        this.evalWindowSize = evalWindowSize;
        this.resolutionStepSize = resolutionStepSize;

        /* mutex robot snapshots */
        for (Ally fielder : fielders) {
            fielderSnaps.add(new RobotSnapshot(fielder));
        }
        for (Foe foe : foes) {
            foeSnaps.add(new RobotSnapshot(foe));
        }
        ballPosWrapper = new RWLockee<>(ball.getPos());

        /* discretize the field */
        grid = new Gridify(new Vec2D(resolutionStepSize, resolutionStepSize),
                new Vec2D(-FIELD_WIDTH / 2, -FIELD_LENGTH / 2), false, false);
        gridOrigin = grid.fromPos(new Vec2D(-FIELD_WIDTH / 2, -FIELD_LENGTH / 2));
        width = grid.numCols(FIELD_WIDTH) + 1;
        height = grid.numRows(FIELD_LENGTH) + 1;

        evalGrid = new Gridify(new Vec2D(evalWindowSize, evalWindowSize),
                new Vec2D(0, 0), false, false);
        evalOrigin = evalGrid.fromPos(new Vec2D(0, 0));
        evalWidth = evalGrid.numCols(width) + 1;
        evalHeight = evalGrid.numRows(height) + 1;

        Vec2D lpA = audienceToPlayer(LEFT_PENALTY_STRETCH.p1);
        Vec2D lpB = audienceToPlayer(LEFT_PENALTY_STRETCH.p2);
        Vec2D rpA = audienceToPlayer(RIGHT_PENALTY_STRETCH.p1);
        Vec2D rpB = audienceToPlayer(RIGHT_PENALTY_STRETCH.p2);

        if (lpA.x > lpB.x) {
            Vec2D tmp = lpA;
            lpA = lpB;
            lpB = tmp;
        }
        if (rpA.x > rpB.x) {
            Vec2D tmp = rpA;
            rpA = rpB;
            rpB = tmp;
        }
        double penaltyWidth = lpA.sub(lpB).mag();
        double penaltyHeight;
        if (lpA.y < 0) {
            penaltyHeight = (new Vec2D(lpA.x, -FIELD_LENGTH / 2)).sub(lpA).mag();
        } else {
            penaltyHeight = (new Vec2D(lpA.x, FIELD_LENGTH / 2)).sub(lpA).mag();
        }
        if (lpA.y < rpA.y) {
            Vec2D lpC = new Vec2D(lpA.x, -FIELD_LENGTH / 2);
            allyPenaltyRegion = new Rect2D(lpC, penaltyWidth, penaltyHeight);
            foePenaltyRegion = new Rect2D(rpA, penaltyWidth, penaltyHeight);
        } else {
            Vec2D rpC = new Vec2D(rpA.x, -FIELD_LENGTH / 2);
            allyPenaltyRegion = new Rect2D(rpC, penaltyWidth, penaltyHeight);
            foePenaltyRegion = new Rect2D(lpA, penaltyWidth, penaltyHeight);
        }
    }


    public ArrayList<Vec2D> getTopNMaxPosWithClearance(int n, double interAllyClearance) {
        ArrayList<Vec2D> potentialMaxPos = getTopNMaxPos(n * 3);
        ArrayList<Vec2D> maxPos = new ArrayList<>();
        ArrayList<Vec2D> backupPos = new ArrayList<>();
        if(potentialMaxPos == null) {
            return null;
        }

        Vec2D prevCandPos = null;
        for(Vec2D candidatePos : potentialMaxPos) {
            if(prevCandPos == null) {
                prevCandPos = candidatePos;
                maxPos.add(candidatePos);
            } else {
                if(candidatePos.sub(prevCandPos).mag() > interAllyClearance) {
                    maxPos.add(candidatePos);
                } else {
                    backupPos.add(candidatePos);
                }
            }
            if(maxPos.size() >= n) break;
        }

        if(maxPos.size() < n) {
            for(Vec2D pos : backupPos) {
                maxPos.add(pos);
                if(maxPos.size() >= n) break;
            }
        }
        return maxPos;
    }


    public ArrayList<Vec2D> getTopNMaxPos(int n) {
        Vec2D[][] localMaxPosArr = localMaxPosWrapper.get();
        double[][] localMaxScoreArr = localMaxScoreWrapper.get();

        if(localMaxPosArr == null || localMaxScoreArr == null) {
            return null;
        }

        ArrayList<Vec2D> topNPos = new ArrayList<>();
        for(int k = 0; k < n; k++) {
            double globalMax = 0.0;
            Vec2D globalMaxPos = null;
            for (int i = 0; i < localMaxPosArr.length; i++) {
                for (int j = 0; j < localMaxPosArr[0].length; j++) {
                    if(!topNPos.contains(localMaxPosArr[i][j])) {
                        if(localMaxScoreArr[i][j] > globalMax) {
                            globalMax = localMaxScoreArr[i][j];
                            globalMaxPos = localMaxPosArr[i][j];
                        }
                    }
                }
            }
            if(globalMaxPos != null) {
                topNPos.add(globalMaxPos);
            }
        }
        return topNPos;
    }


    /* certain element of this 2dArray might be null, make sure to check it */
    public Vec2D[][] getLocalMaxPosArr() {
        update();
        return localMaxPosWrapper.get();
    }

    public double[][] getPMF() {
        update();
        return pmfWrapper.get();
    }


    public double getProb(double[][] pmf, Vec2D pos) {
        int[] idx = grid.fromPos(pos);
        if(pmf == null) return 0.0;
        return pmf[idx[0]][idx[1]];
    }

    public void update() {
        /* Update robot states snapshot */
        ListIterator<RobotSnapshot> fielderSnapIt = fielderSnaps.listIterator();
        while (fielderSnapIt.hasNext()) {
            Robot fielder = fielders.get(fielderSnapIt.nextIndex());
            fielderSnapIt.next().update(fielder);
        }

        ListIterator<RobotSnapshot> foeSnapIt = foeSnaps.listIterator();
        while (foeSnapIt.hasNext()) {
            Robot foe = foes.get(foeSnapIt.nextIndex());
            foeSnapIt.next().update(foe);
        }

        /* Update ball pos */
        ballPosWrapper.set(ball.getPos());
    }


    /**
     * Calculate the pmf of optimal gap region for passing and attacking,
     * the pmf are indexed by gridIdx instead of coordinates
     */
    @Override
    protected void calcProb() {
        // long t0 = System.currentTimeMillis();
        Vec2D ballPos = ballPosWrapper.get();

        double[][] pmf = new double[width][height];
        Vec2D[][] localMaxPos = new Vec2D[evalWidth][evalHeight];
        double[][] localMax = new double[evalWidth][evalHeight]; // all-zero by default
        double[][] localMaxScore = new double[evalWidth][evalHeight]; // all-zero by default

        for (int gridX = gridOrigin[0]; gridX < width; gridX++) {
            for (int gridY = gridOrigin[1]; gridY < height; gridY++) {
                Vec2D pos = grid.fromInd(gridX, gridY);
                double prob = 1.0;

                /* mask forbidden and unlikely regions */
                // Penalty Region
                if (allyPenaltyRegion.isInside(pos) || foePenaltyRegion.isInside(pos)) {
                    pmf[gridX][gridY] = 0.0;
                    continue;
                }

                /* away from foe bots */
                double minDist = Double.MAX_VALUE;
                for (RobotSnapshot foeSnap : foeSnaps) {
                    double dist = pos.sub(foeSnap.getPos()).mag();
                    if (dist < minDist) {
                        minDist = dist;
                    }
                }
                double ratio = minDist / responseRange;
                if (ratio < 1.0) {
                    prob *= ratio;
                }

                /* make it harder to be intercept between foe and ball */
                double distFoeToIntercept = Double.MAX_VALUE;
                Vec2D nearestFoePos = null;
                /* find nearest foe dist to the potential passing line */
                for (RobotSnapshot foeSnap : foeSnaps) {
                    // enforce correct direction
                    double dist = foeSnap.getPos().distToLine(new Line2D(pos, ballPos));
                    if (dist < distFoeToIntercept) {
                        distFoeToIntercept = dist;
                        nearestFoePos = foeSnap.getPos();
                    }
                }

                if (nearestFoePos != null &&
                        Math.abs(normAng(nearestFoePos.sub(ballPos).toPlayerAngle()
                                - pos.sub(ballPos).toPlayerAngle())) < 60) // To-do: magic number
                {
                    /* if greater, prob should be 100% for assuming foe can't intercept (indeed an assumption to make implementation simpler)*/
                    if (distFoeToIntercept < interceptRange) {
                        prob *= distFoeToIntercept / interceptRange;
                    }
                }

                double distToFrontEnd = FIELD_LENGTH / 2 - pos.y;
                if (pos.y > ballPos.y) {
                    /* make it keep a balanced position between ball and frontEndLine */
                    double distToBall = pos.sub(ballPos).mag();
                    double midDist = (distToFrontEnd + distToBall) / 2;
                    prob *= Math.min(distToFrontEnd, distToBall) / midDist;
                } else {
                    /* make it go as front as possible */
                    prob *= (FIELD_LENGTH - distToFrontEnd) / FIELD_LENGTH;
                    prob *= 0.5; // lower this prob so that position satisfying pos.y > ballPos.y will have greater prob
                }


                pmf[gridX][gridY] = prob;

                int[] evalIdx = evalGrid.fromPos(new Vec2D(gridX, gridY));
                if(prob > localMax[evalIdx[0]][evalIdx[1]]) {
                    localMax[evalIdx[0]][evalIdx[1]] = prob;
                    localMaxPos[evalIdx[0]][evalIdx[1]] = pos;
                }
                localMaxScore[evalIdx[0]][evalIdx[1]] += prob;
            }
        }

        pmfWrapper.set(pmf);
        localMaxPosWrapper.set(localMaxPos);
        localMaxScoreWrapper.set(localMax);
    }

    public int[] getIdxFromPos(Vec2D pos) {
        return grid.fromPos(pos);
    }

}
