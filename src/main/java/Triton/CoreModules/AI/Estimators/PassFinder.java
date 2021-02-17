package Triton.CoreModules.AI.Estimators;

import Triton.CoreModules.AI.Estimators.TimeEstimator.BallMovement;
import Triton.CoreModules.AI.Estimators.TimeEstimator.RobotMovement;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.Misc.ModulePubSubSystem.FieldPublisher;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Publisher;
import Triton.Misc.ModulePubSubSystem.Subscriber;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static Triton.Config.GeometryConfig.FIELD_LENGTH;
import static Triton.Config.GeometryConfig.FIELD_WIDTH;
import static Triton.Config.GeometryConfig.GOAL_LENGTH;

public class PassFinder extends GapFinder {

    private static final double C1_WEIGHT = 2.0;

    private static final int C2_INTERVAL = 5;
    private static final double C2_WEIGHT = 1.0;

    private static final double C3_T_MIN = 0.1;
    private static final double C3_T_MAX = 0.3;
    private static final double C3_DEV = 0.1;

    private static final double C4_MAX_DIST = 3000.0;
    private static final double C4_MIN_DIST = 1500.0;
    private static final double C4_DEV = 500.0;

    private static final double C5_MAX_DIST = 500.0;
    private static final double C5_DEV = 100.0;

    private static final double PASS_VEL = 2.5;
    private static final double SHOOT_VEL = 4.0;
    private static final double ROBOT_PADDING = 180.0;
    private static final double FRONT_PADDING = 100.0;

    private static final int G1_GOAL_INTERVAL = 3;
    private static final int G1_INTERCEPT_INTERVAL = 3;
    private static final double G1_WEIGHT = 2.5;

    private static final double G2_MEAN = 20.0;
    private static final double G2_DEV = 40.0;
    private static final double G2_WEIGHT = 2.5;

    private static final int G3_GOAL_INTERVAL = 5;
    private static final double G3_ONE_SHOT_ANGLE = 20;
    private static final double G3_WEIGHT = 2.5;

    private volatile boolean fixCandidate = false;
    private volatile Integer candidate = null;
    private volatile Integer passer = null;

    protected final Publisher<double[][]> gPub;
    protected final Subscriber<double[][]> gSub;
    protected final Publisher<int[][]> rPub;
    protected final Subscriber<int[][]> rSub;

    protected final Publisher<ArrayList<Vec2D>> fielderVelListPub;
    protected final Subscriber<ArrayList<Vec2D>> fielderVelListSub;
    protected final Publisher<ArrayList<Vec2D>> foeVelListPub;
    protected final Subscriber<ArrayList<Vec2D>> foeVelListSub;

    protected final Publisher<ArrayList<Double>> fielderAngListPub;
    protected final Subscriber<ArrayList<Double>> fielderAngListSub;
    protected final Publisher<ArrayList<Double>> foeAngListPub;
    protected final Subscriber<ArrayList<Double>> foeAngListSub;

    private ArrayList<Vec2D> fielderPosList;
    private ArrayList<Vec2D> fielderVelList;
    private ArrayList<Double> fielderAngList;
    private ArrayList<Vec2D> foePosList;
    private ArrayList<Vec2D> foeVelList;
    private ArrayList<Double> foeAngList;
    private Vec2D ballPos;

    public PassFinder(RobotList<Ally> fielders, RobotList<Foe> foes, Ball ball) {
        this(fielders, foes, ball, 400, 20);
    }

    public PassFinder(RobotList<Ally> fielders, RobotList<Foe> foes, Ball ball,
                     int resolutionStepSize, int evalWindowSize) {
        super(fielders, foes, ball, resolutionStepSize, evalWindowSize);

        String topicName = this.getClass().getSimpleName();

        fielderVelListPub = new FieldPublisher<>(topicName, "FielderVelocities" + this.toString(), null);
        fielderVelListSub = new FieldSubscriber<>(topicName, "FielderVelocities" + this.toString());

        foeVelListPub = new FieldPublisher<>(topicName, "FoeVelocities" + this.toString(), null);
        foeVelListSub = new FieldSubscriber<>(topicName, "FoeVelocities" + this.toString());

        fielderAngListPub = new FieldPublisher<>(topicName, "FielderAngles" + this.toString(), null);
        fielderAngListSub = new FieldSubscriber<>(topicName, "FielderAngles" + this.toString());

        foeAngListPub = new FieldPublisher<>(topicName, "FoeAngles" + this.toString(), null);
        foeAngListSub = new FieldSubscriber<>(topicName, "FoeAngles" + this.toString());

        rPub = new FieldPublisher<>(topicName, "Receiver", null);
        rSub = new FieldSubscriber<>(topicName, "Receiver");

        gPub = new FieldPublisher<>(topicName, "G-prob", null);
        gSub = new FieldSubscriber<>(topicName, "G-prob");

        try {
            fielderVelListSub.subscribe(TIMEOUT);
            foeVelListSub.subscribe(TIMEOUT);
            fielderAngListSub.subscribe(TIMEOUT);
            foeAngListSub.subscribe(TIMEOUT);
            rSub.subscribe(TIMEOUT);
            gSub.subscribe(TIMEOUT);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double[][] getPMF() {
        supplyInfo();
        return pmfSub.getMsg();
    }

    public int[][] getR() {
        supplyInfo();
        return rSub.getMsg();
    }

    public double[][] getGProb() {
        supplyInfo();
        return gSub.getMsg();
    }

    private void supplyInfo() {
        ArrayList<Vec2D> fielderPosList = new ArrayList<>();
        ArrayList<Vec2D> fielderVelList = new ArrayList<>();
        ArrayList<Double> fielderAngList = new ArrayList<>();
        for (Ally fielder : fielders) {
            fielderPosList.add(fielder.getPos());
            fielderVelList.add(fielder.getVel());
            fielderAngList.add(fielder.getDir());
        }
        ArrayList<Vec2D> foePosList = new ArrayList<>();
        ArrayList<Vec2D> foeVelList = new ArrayList<>();
        ArrayList<Double> foeAngList = new ArrayList<>();
        for (Foe foe : foes) {
            foePosList.add(foe.getPos());
            foeVelList.add(foe.getVel());
            foeAngList.add(foe.getDir());
        }
        Vec2D ballPos = ball.getPos();

        fielderPosListPub.publish(fielderPosList);
        fielderVelListPub.publish(fielderVelList);
        fielderAngListPub.publish(fielderAngList);
        foePosListPub.publish(foePosList);
        foeVelListPub.publish(foeVelList);
        foeAngListPub.publish(foeAngList);
        ballPosPub.publish(ballPos);
    }



    @Override
    protected void calcProb() {
        double[][] pmf = new double[width][height];
        double[][] gProbs = new double[width][height];
        int[][] R = new int[width][height];
        Vec2D[][] localMaxPos = new Vec2D[evalWidth][evalHeight];
        double[][] localMax = new double[evalWidth][evalHeight];
        double[][] localMaxScore = new double[evalWidth][evalHeight];

        for(int i = 0; i < evalWidth; i++) {
            for(int j = 0; j < evalHeight; j++) {
                localMax[i][j] = 0.0;
            }
        }

        fielderPosList = fielderPosListSub.getMsg();
        fielderVelList = fielderVelListSub.getMsg();
        fielderAngList = fielderAngListSub.getMsg();
        foePosList = foePosListSub.getMsg();
        foeVelList = foeVelListSub.getMsg();
        foeAngList = foeAngListSub.getMsg();
        ballPos = ballPosSub.getMsg();

        if (fielderPosList == null || foePosList == null || fielderVelList == null || foeVelList == null ||
                fielderAngList == null || foeAngList == null || ballPos == null) {
            return;
        }

        long t0 = System.currentTimeMillis();
        double[] passMaxPair = BallMovement.calcMaxDist(PASS_VEL); // Precompute for acceleration
        double[] shootMaxPair = BallMovement.calcMaxDist(SHOOT_VEL);

        /* Find passer (default as closest fielder) **/
        double minDist = Double.MAX_VALUE;
        passer = 0;
        for(int i = 0; i < fielders.size(); i++) {
            double temp;
            if ((temp = fielderPosList.get(i).sub(ballPos).mag()) < minDist) {
                minDist = temp;
                passer = i;
            }
        }

        for (int gridX = gridOrigin[0]; gridX < width; gridX++) {
            for (int gridY = gridOrigin[1]; gridY < height; gridY++) {
                Vec2D pos = grid.fromInd(gridX, gridY);

                double ballDist = ballPos.sub(pos).mag();
                double ballTime = BallMovement.calcETAFast(PASS_VEL, ballDist, passMaxPair);

                /* c3: The pass is long enough for R to react and receive the pass robustly **/
                double c3;
                if (ballTime < C3_T_MIN) {
                    c3 = - Double.MAX_VALUE;
                } else {
                    c3 = Math.min(0, ballTime - C3_T_MAX) / C3_DEV;
                }

                /* c4: The pass is short enough to be performed accurately **/
                if (ballDist > C4_MAX_DIST) continue;
                double c4 = Math.min(0, C4_MIN_DIST - ballDist) / C4_DEV;

                /* c5: Location x is reliable for pass reception **/
                if (allyPenaltyRegion.isInside(pos) || foePenaltyRegion.isInside(pos)) continue;
                double penaltyDist = Math.min(allyPenaltyRegion.distTo(pos), foePenaltyRegion.distTo(pos));
                double xDist = Math.min(Math.abs(pos.x - FIELD_WIDTH / 2), Math.abs(pos.x + FIELD_WIDTH / 2));
                double yDist = Math.min(Math.abs(pos.y - FIELD_LENGTH / 2), Math.abs(pos.y + FIELD_LENGTH / 2));
                double c5 = Math.min(0, penaltyDist - C5_MAX_DIST) / C5_DEV +
                        Math.min(0, xDist - C5_MAX_DIST) / C5_DEV +
                        Math.min(0, yDist - C5_MAX_DIST) / C5_DEV;

                /* c2 : No opponent intercepts the pass. **/
                Vec2D ballToPos = pos.sub(ballPos).scale(1.0 / C2_INTERVAL);
                double c2 = Double.MAX_VALUE;

                for (int i = 1; i < C2_INTERVAL; i++) {
                    Vec2D path = ballToPos.scale(i);
                    Vec2D interceptPos = ballPos.add(path);
                    double ballTime_ = BallMovement.calcETAFast(PASS_VEL, path.mag(), passMaxPair);
                    double foeTime_ = Double.MAX_VALUE;
                    for (int j = 0; j < foes.size(); j++) {
                        Vec2D foePos = foePosList.get(j);
                        double[] angleRange = angleRange(foePos, ballPos);
                        if (foePos.sub(ballPos).mag() - FRONT_PADDING < pos.sub(ballPos).mag() &&
                            angleBetween(path.toPlayerAngle(), angleRange)) {
                            foeTime_ = 0;
                            continue;
                        }
                        double ETA = calcETA(false, j, interceptPos);
                        foeTime_ = Math.min(ETA, foeTime_);
                    }
                    c2 = Math.min(foeTime_ - ballTime_, c2);
                }
                c2 *= C2_WEIGHT;

                /* g1: Shots from x can reach the opposing goal faster than their goalkeeper can block them. **/
                Vec2D leftGoal = new Vec2D(-GOAL_LENGTH / 2, FIELD_LENGTH / 2);
                Vec2D goalSeg = new Vec2D(GOAL_LENGTH, 0).scale(1.0 / G1_GOAL_INTERVAL);
                double g1 = Double.MAX_VALUE;

                for (int i = 1; i < G1_GOAL_INTERVAL; i++) {
                    double g1_ = Double.MAX_VALUE;
                    Vec2D goal = leftGoal.add(goalSeg.scale(i));
                    Vec2D xToGoal = goal.sub(pos).scale(1.0 / G1_INTERCEPT_INTERVAL);
                    for (int j = 1; j < G1_INTERCEPT_INTERVAL; j++) {
                        Vec2D path = xToGoal.scale(j);
                        Vec2D interceptPos = pos.add(path);
                        double ballTime_ = BallMovement.calcETAFast(SHOOT_VEL, path.mag(), shootMaxPair);
                        double foeTime_ = Double.MAX_VALUE;
                        for (int k = 0; k < foes.size(); k++) {
                            Vec2D foePos = foePosList.get(k);
                            double[] angleRange = angleRange(foePos, pos);
                            if (foePos.sub(pos).mag() - FRONT_PADDING < goal.sub(pos).mag() &&
                                    angleBetween(path.toPlayerAngle(), angleRange)) {
                                foeTime_ = 0;
                                continue;
                            }
                            double ETA = calcETA(false, k, interceptPos);
                            foeTime_ = Math.min(ETA, foeTime_);
                        }
                        g1_ = Math.min(foeTime_ - ballTime_, g1_);
                    }
                    g1 = Math.min(g1_, g1);
                }
                if(g1 < 0) g1 *= G1_WEIGHT;

                /* g2: There is a wide enough open angle θ from x to the opposing goal **/
                Vec2D rightGoal = leftGoal.add(GOAL_LENGTH, 0);
                double openAngle = angDiff(rightGoal.sub(pos).toPlayerAngle(), leftGoal.sub(pos).toPlayerAngle());
                double g2 = (openAngle - G2_MEAN) / G2_DEV * G2_WEIGHT;

                double maxProb = 0.0;
                double maxGProb = 0.0;
                int receiver = 0;

                for (int candidate = 0; candidate < fielders.size(); candidate++) {
                    /* Fix candidate, run only one iteration */
                    if (fixCandidate) candidate = this.candidate;

                    /* Skip passer **/
                    if (candidate == passer) continue;

                    /* c1 : No opponent can reach x faster than R can. **/
                    double c1 = 0.0;
                    double receiverTime = calcETA(true, candidate, pos);
                    double foeTime = Double.MAX_VALUE;
                    for (int i = 0; i < foes.size(); i++) {
                        // Find closest foe
                        double ETA = calcETA(false, i, pos);
                        foeTime = Math.min(ETA, foeTime);
                    }
                    c1 = foeTime - receiverTime;
                    c1 *= C1_WEIGHT;

                    /* g3: R will have enough time to take a shot before opponents steal the ball **/
                    double g3 = 0.0;
                    Vec2D rPos = fielderPosList.get(candidate);
                    for (int i = 0; i <= G3_GOAL_INTERVAL; i++) {
                        Vec2D goal = leftGoal.add(goalSeg.scale(i));
                        if (goal.sub(rPos).mag() > goal.sub(pos).mag() &&
                            angDiff(goal.sub(pos).toPlayerAngle(),
                                    pos.sub(rPos).toPlayerAngle()) < G3_ONE_SHOT_ANGLE)
                            g3 = G3_WEIGHT;
                    }

                    double c = c1 + c2 + c3 + c4 + c5;
                    double g = g1 + g2 + g3;
                    // double prob = (1 / (1 + Math.exp(-c))) * (1 / (1 + Math.exp(-g)));

                    double score = c + g;
                    double prob = (1 / (1 + Math.exp(-score)));
                    double gProb = (1 / (1 + Math.exp(-g)));

                    if (prob > maxProb) {
                        maxProb = prob;
                        receiver = candidate;
                    }

                    if (gProb > maxGProb) {
                        maxGProb = gProb;
                    }

                    if (fixCandidate) break;
                }

                pmf[gridX][gridY] = maxProb;
                gProbs[gridX][gridY] = maxGProb;

                if (fixCandidate) receiver = candidate;
                R[gridX][gridY] = receiver;
                int[] evalIdx = evalGrid.fromPos(new Vec2D(gridX, gridY));
                if(maxProb > localMax[evalIdx[0]][evalIdx[1]]) {
                    localMax[evalIdx[0]][evalIdx[1]] = maxProb;
                    localMaxPos[evalIdx[0]][evalIdx[1]] = pos;
                }
                localMaxScore[evalIdx[0]][evalIdx[1]] += maxProb;
            }
        }

        rPub.publish(R);
        gPub.publish(gProbs);
        pmfPub.publish(pmf);
        localMaxPosPub.publish(localMaxPos);
        localMaxScorePub.publish(localMax);
    }

    private double calcETA(boolean ally, int ID, Vec2D dest) {
        Vec2D vel, pos;
        double ang;
        if (ally) {
            vel = fielderVelList.get(ID);
            pos = fielderPosList.get(ID);
            ang = fielderAngList.get(ID);
        } else {
            vel = foeVelList.get(ID);
            pos = foePosList.get(ID);
            ang = foeAngList.get(ID);
        }
        return RobotMovement.calcETA(ang, vel, dest, pos);
    }

    public void fixCandidate(int candidate) {
        this.fixCandidate = true;
        this.candidate = candidate;
    }

    private static double[] angleRange(Vec2D robotPos, Vec2D start) {
        Vec2D robotLeftPos = robotPos.add(-ROBOT_PADDING, 0);
        Vec2D robotRightPos = robotPos.add(ROBOT_PADDING, 0);
        Vec2D robotUpPos = robotPos.add(0, ROBOT_PADDING);
        Vec2D robotDownPos = robotPos.add(0, -ROBOT_PADDING);
        double[] angles = new double[] {robotLeftPos.sub(start).toPlayerAngle(),
                robotRightPos.sub(start).toPlayerAngle(), robotUpPos.sub(start).toPlayerAngle(),
                robotDownPos.sub(start).toPlayerAngle()};
        int[][] combination = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {1, 3}, {2, 3}};
        int[] maxIdx = combination[0];
        double maxDiff = 0;
        for (int[] idx : combination) {
            double diff = angDiff(angles[idx[0]], angles[idx[1]]);
            if (diff > maxDiff) {
                maxDiff = diff;
                maxIdx = idx;
            }
        }
        return new double[] {angles[maxIdx[0]], angles[maxIdx[1]]};
    }

    private static boolean angleBetween(double angle, double[] angleRange) {
        double totalDiff = angDiff(angleRange[0], angleRange[1]);
        return angDiff(angle, angleRange[0]) < totalDiff && angDiff(angle, angleRange[1]) < totalDiff;
    }

    private static double angDiff(double a1, double a2) {
        return Math.abs(PerspectiveConverter.calcAngDiff(a1, a2));
    }

    /**
     * @return a tuple of information needed by pass
     */
    public PassInfo evalPass() {
        supplyInfo();
        try {
            double[][] pmf = pmfSub.getMsg();
            if (pmf == null) return null;
            int[][] receiver = rSub.getMsg();

            Vec2D topPos = getTopNMaxPos(1).get(0);
            int[] idx = getIdxFromPos(topPos);
            double maxProb = pmf[idx[0]][idx[1]];
            int bestReceiver = receiver[idx[0]][idx[1]];

            PassInfo info = new PassInfo(fielders, foes, ball);
            info.setInfo(passer, bestReceiver, fielderPosList.get(passer), topPos, maxProb);
            return info;
        } catch (Exception e) {
            fixCandidate = false;
            return null;
        }
    }
}
