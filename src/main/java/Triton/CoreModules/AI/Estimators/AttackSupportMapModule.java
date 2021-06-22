package Triton.CoreModules.AI.Estimators;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.*;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.Misc.Math.Geometry.Line2D;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.RWLockee;
import Triton.SoccerObjects;

import java.util.ArrayList;

import static Triton.Config.GlobalVariblesAndConstants.GvcGeometry.*;
import static Triton.Misc.Math.Coordinates.PerspectiveConverter.audienceToPlayer;
import static Triton.Misc.Math.Coordinates.PerspectiveConverter.normAng;

public class AttackSupportMapModule extends ProbMapModule {

    private final double botCoveredRange = 1000.0;
    private final double behindBallLoweringProbFactor = 0.75;


    protected RWLockee<ArrayList<RobotSnapshot>> attackerSnapsWrapper = new RWLockee<>(null);

    public AttackSupportMapModule(SoccerObjects soccerObjects) {
        super(soccerObjects.fielders, soccerObjects.foes, soccerObjects.ball);
    }

    public AttackSupportMapModule(SoccerObjects soccerObjects, int resolutionStepSize, int evalWindowSize) {
        super(soccerObjects.fielders, soccerObjects.foes, soccerObjects.ball, resolutionStepSize, evalWindowSize);
    }

    public AttackSupportMapModule(RobotList<Ally> fielders, RobotList<Foe> foes, Ball ball) {
        super(fielders, foes, ball, 100, 10);
    }


    public void setDecoys(RobotList<Ally> decoys) {
        ArrayList<RobotSnapshot> attackerSnaps = new ArrayList<>();
        for(Ally fielder : fielders) {
            if(!decoys.contains(fielder)) {
                attackerSnaps.add(new RobotSnapshot(fielder));
            }
        }
        attackerSnapsWrapper.set(attackerSnaps);
    }



//    /**
//     * Calculate the pmf of optimal regions for remaining robots not on the attackers list to support the attack,
//     * the pmf are indexed by gridIdx instead of coordinates
//     */
//    protected void calcProb() {
//        // long t0 = System.currentTimeMillis();
//        Vec2D ballPos = ballPosWrapper.get();
//
//        double[][] pmf = new double[width][height];
//        Vec2D[][] localMaxPos = new Vec2D[evalWidth][evalHeight];
//        double[][] localMax = new double[evalWidth][evalHeight]; // all-zero by default
//        double[][] localMaxScore = new double[evalWidth][evalHeight]; // all-zero by default
//
//        for (int gridX = gridOrigin[0]; gridX < width; gridX++) {
//            for (int gridY = gridOrigin[1]; gridY < height; gridY++) {
//                Vec2D pixelPos = grid.fromInd(gridX, gridY);
//                double prob = 1.0;
//
//                /* mask forbidden and unlikely regions */
//                // Penalty Region
//                if (allyPenaltyRegion.isInside(pixelPos) || foePenaltyRegion.isInside(pixelPos)) {
//                    pmf[gridX][gridY] = 0.0;
//                    continue;
//                }
//
//                /* away from foe bots */
//                double minDist = Double.MAX_VALUE;
//                for (RobotSnapshot foeSnap : foeSnaps) {
//                    double dist = pixelPos.sub(foeSnap.getPos()).mag();
//                    if (dist < minDist) {
//                        minDist = dist;
//                    }
//                }
//                double ratio = minDist / botCoveredRange;
//                if (ratio < 1.0) {
//                    prob *= ratio;
//                }
//
//                /* away from ally attacker bots on the attacking path */
//                ArrayList<RobotSnapshot> attackerSnaps = attackerSnapsWrapper.get();
//                if(attackerSnaps != null && attackerSnaps.size() > 0) {
//                    minDist = Double.MAX_VALUE;
//                    for (RobotSnapshot atkSnap : attackerSnaps) {
//                        double dist = pixelPos.sub(atkSnap.getPos()).mag();
//                        if (dist < minDist) {
//                            minDist = dist;
//                        }
//                    }
//                    ratio = minDist / botCoveredRange;
//                    if (ratio < 1.0) {
//                        prob *= ratio;
//                    }
//                }
//
//
//
////                /* make it keep a weighted balanced position between ball and frontEndLine */
////                double distToFrontEnd = FIELD_LENGTH / 2 - pixelPos.y;
////                if(pixelPos.y > ballPos.y) {
////                    double distToBall = pixelPos.sub(ballPos).mag();
////                    double midDist = (distToFrontEnd + distToBall) / 2;
////                    prob *= Math.min(distToFrontEnd, distToBall) / midDist;
////                } else {
////                    /* if pixel is behind the ball, set a low prob so robot tends to go as front as possible */
////                    prob *= Math.abs(FIELD_LENGTH - distToFrontEnd) / FIELD_LENGTH;
////                    prob *= behindBallLoweringProbFactor; // lower this prob so that position satisfying pos.y > ballPos.y will have greater prob
////                }
//
//
//
//
//
//
//
//                /* ======================================================= */
//                pmf[gridX][gridY] = Math.abs(prob);
//                int[] evalIdx = evalGrid.fromPos(new Vec2D(gridX, gridY));
//                if(prob > localMax[evalIdx[0]][evalIdx[1]]) {
//                    localMax[evalIdx[0]][evalIdx[1]] = prob;
//                    localMaxPos[evalIdx[0]][evalIdx[1]] = pixelPos;
//                }
//                localMaxScore[evalIdx[0]][evalIdx[1]] += prob;
//            }
//        }
//
//        pmfWrapper.set(pmf);
//        localMaxPosWrapper.set(localMaxPos);
//        localMaxScoreWrapper.set(localMax);
//    }
    
    
    
    
        /**
          * Calculate the pmf of optimal gap region for passing and attacking,
          * the pmf are indexed by gridIdx instead of coordinates
          */
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
                double ratio = minDist / botCoveredRange;
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
                    if (distFoeToIntercept < botCoveredRange) {
                        prob *= distFoeToIntercept / botCoveredRange;
                    }
                }

                /* make it keep a balanced position between ball and frontEndLine  or  make it go as front as possible */
                double distToFrontEnd = FIELD_LENGTH / 2 - pos.y;
                if (pos.y > ballPos.y) {
                    /* make it keep a balanced position between ball and frontEndLine */
                    double distToBall = pos.sub(ballPos).mag();
                    double midDist = (distToFrontEnd + distToBall) / 2;
                    prob *= Math.min(distToFrontEnd, distToBall) / midDist;
                } else {
                    /* make it go as front as possible */
                    prob *= Math.abs(FIELD_LENGTH - distToFrontEnd) / FIELD_LENGTH;
                    prob *= 0.5; // lower this prob so that position satisfying pos.y > ballPos.y will have greater prob
                }





                /* ======================================================= */
                pmf[gridX][gridY] = Math.abs(prob);
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
}
