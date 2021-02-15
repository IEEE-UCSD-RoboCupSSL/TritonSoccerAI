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

public class PassFinder extends GapFinder {

    private static final double C1_WEIGHT = 1.0;

    private static final int C2_INTERVAL = 5;
    private static final double C2_WEIGHT = 1.0;

    private static final double C3_T_MIN = 0.1;
    private static final double C3_T_MAX = 0.3;
    private static final double C3_MEAN = 0.1;

    private static final double C4_MAX_DIST = 3000.0;
    private static final double C4_MIN_DIST = 2000.0;
    private static final double C4_MEAN = 500.0;

    private static final double C5_MAX_DIST = 500.0;
    private static final double C5_MEAN = 100.0;

    private static final double PASS_VEL = 2.5;
    private static final double SHOOT_VEL = 4.0;
    private static final double ROBOT_PADDING = 120.0;
    private static final double FRONT_PADDING = 100.0;

    private volatile boolean fixCandidate = false;
    private volatile Integer candidate = null;

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
        this(fielders, foes, ball, 50, 10);
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

        try {
            fielderVelListSub.subscribe(TIMEOUT);
            foeVelListSub.subscribe(TIMEOUT);
            fielderAngListSub.subscribe(TIMEOUT);
            foeAngListSub.subscribe(TIMEOUT);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double[][] getPMF() {
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

        return pmfSub.getMsg();
    }



    @Override
    protected void calcProb() {
        double[][] pmf = new double[width][height];
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
        double[] maxPair = BallMovement.calcMaxDist(PASS_VEL); // Precompute for acceleration

        /* Find passer (default as closest fielder) **/
        double minDist = Double.MAX_VALUE;
        int passer = 0;
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
                double ballTime = BallMovement.calcETAFast(PASS_VEL, ballDist, maxPair);

                /* c3: The pass is long enough for R to react and receive the pass robustly **/
                if (ballTime < C3_T_MIN) continue;
                double c3 = Math.min(0, ballTime - C3_T_MAX) / C3_MEAN;

                /* c4: The pass is short enough to be performed accurately **/
                if (ballDist > C4_MAX_DIST) continue;
                double c4 = Math.min(0, C4_MIN_DIST - ballDist) / C4_MEAN;

                /* c5: Location x is reliable for pass reception **/
                if (allyPenaltyRegion.isInside(pos) || foePenaltyRegion.isInside(pos)) continue;
                double penaltyDist = Math.min(allyPenaltyRegion.distTo(pos), foePenaltyRegion.distTo(pos));
                double xDist = Math.min(Math.abs(pos.x - worldWidth / 2), Math.abs(pos.x + worldWidth / 2));
                double yDist = Math.min(Math.abs(pos.y - worldLength / 2), Math.abs(pos.y + worldLength / 2));
                double c5 = Math.min(0, penaltyDist - C5_MAX_DIST) / C5_MEAN +
                        Math.min(0, xDist - C5_MAX_DIST) / C5_MEAN +
                        Math.min(0, yDist - C5_MAX_DIST) / C5_MEAN;

                /* c2 : No opponent intercepts the pass. **/
                Vec2D ballToPos = pos.sub(ballPos).scale(1.0 / C2_INTERVAL);
                double c2 = Double.MAX_VALUE;

                for (int i = 1; i < C2_INTERVAL; i++) {
                    Vec2D path = ballToPos.scale(i);
                    Vec2D interceptPos_ = ballPos.add(path);
                    double ballTime_ = BallMovement.calcETAFast(PASS_VEL, path.mag(), maxPair);
                    double foeTime_ = Double.MAX_VALUE;
                    for (int j = 0; j < foes.size(); j++) {
                        Vec2D foePos = foePosList.get(j);
                        double[] angleRange = angleRange(foePos, ballPos);
                        double ETA;
                        if (foePos.sub(ballPos).mag() - FRONT_PADDING < pos.sub(ballPos).mag() &&
                            angleBetween(path.toPlayerAngle(), angleRange)) {
                            ETA = 0;
                        } else {
                            ETA = calcETA(false, j, interceptPos_);
                        }
                        foeTime_ = Math.min(ETA, foeTime_);
                    }
                    c2 = Math.min(foeTime_ - ballTime_, c2);
                }
                c2 *= C2_WEIGHT;

                /* g1: Shots from x can reach the opposing goal faster than their goalkeeper can block them. **/
                //Vec2D goal = new Vec2D(0, worldLength / 2);
                //Vec2D posToGoal = goal.sub(pos).scale(1.0)


                double c1 = 0;
                for (int candidate = 0; candidate < fielders.size(); candidate++) {
                    /* Fix candidate, run only one iteration */
                    if (fixCandidate) candidate = this.candidate;

                    /* Skip passer **/
                    if (candidate == passer) continue;

                    /* c1 : No opponent can reach x faster than R can. **/
                    double receiverTime = calcETA(true, candidate, pos);
                    double foeTime = Double.MAX_VALUE;
                    for (int i = 0; i < foes.size(); i++) {
                        // Find closest foe
                        double ETA = calcETA(false, i, pos);
                        foeTime = Math.min(ETA, foeTime);
                    }
                    c1 = (foeTime - receiverTime) * C1_WEIGHT;

                    if (fixCandidate) break;
                }

                double c = c1 + c2 + c3 + c4 + c5;
                double prob = 1 / (1 + Math.exp(-c)); // Sigmoid

                pmf[gridX][gridY] = prob;
                int[] evalIdx = evalGrid.fromPos(new Vec2D(gridX, gridY));
                if(prob > localMax[evalIdx[0]][evalIdx[1]]) {
                    localMax[evalIdx[0]][evalIdx[1]] = prob;
                    localMaxPos[evalIdx[0]][evalIdx[1]] = pos;
                }
                localMaxScore[evalIdx[0]][evalIdx[1]] += prob;
            }
        }

        //System.out.println(System.currentTimeMillis() - t0);

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
        if (angDiff(angles[0], angles[1]) < angDiff(angles[2], angles[3])) {
            return new double[] {angles[2], angles[3]};
        } else {
            return new double[] {angles[0], angles[1]};
        }
    }

    private static boolean angleBetween(double angle, double[] angleRange) {
        double totalDiff = angDiff(angleRange[0], angleRange[1]);
        return angDiff(angle, angleRange[0]) < totalDiff && angDiff(angle, angleRange[1]) < totalDiff;
    }

    private static double angDiff(double a1, double a2) {
        return Math.abs(PerspectiveConverter.calcAngDiff(a1, a2));
    }
}
