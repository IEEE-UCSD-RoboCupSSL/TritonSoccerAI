package triton.coreModules.ai.estimators;

import triton.coreModules.ai.estimators.timeEstimator.BallMovement;
import triton.coreModules.ai.estimators.timeEstimator.RobotMovement;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.RobotSnapshot;
import triton.misc.math.linearAlgebra.Vec2D;
import org.javatuples.Pair;

import java.util.ArrayList;

public class PassInfo {

    private static final double DIST_MEAN = 500.0;
    private static final double DIST_RANGE = 500.0;
    private static final double WEIGHT_MIN = 1.0;
    private static final double WEIGHT_MAX = 2.2;

    private static final double MAX_KICK_VEL = 4.0;
    private static final double MIN_KICK_VEL = 1.0;
    private static final double GRAVITY = 9.81;
    private static final double KICK_Z_FACTOR = 1.0;
    private static final double KICK_Z_THRESHOLD = 100.0;

    private Ally passer;
    private Ally receiver;
    private ArrayList<RobotSnapshot> robotSnaps;
    private Vec2D passingPos;
    private Vec2D receivingPos;
    private double prob;

    public void setInfo(Ally passer, Ally receiver,
                        Vec2D passingPos, Vec2D receivingPos, double prob) {
        this.passer = passer;
        this.receiver = receiver;
        this.passingPos = passingPos;
        this.receivingPos = receivingPos;
        this.prob = prob;
    }

    public void setRobots(ArrayList<RobotSnapshot> fielderSnaps, ArrayList<RobotSnapshot> foeSnaps) {
        this.robotSnaps = new ArrayList<>(fielderSnaps);
        this.robotSnaps.addAll(foeSnaps);
    }

    public double getMaxProb() {
        return prob;
    }

    public Vec2D getOptimalPassingPos() {
        return passingPos;
    }

    public Ally getOptimalReceiver() {
        return receiver;
    }

    public Vec2D getOptimalReceivingPos() {
        return receivingPos;
    }

    private static double timeWeight(double dist) {
        return (1 / (1 + Math.exp(-(dist - DIST_MEAN) / DIST_RANGE))) * (WEIGHT_MAX - WEIGHT_MIN) + WEIGHT_MIN;
    }

    public Pair<Vec2D, Boolean> getKickDecision() {
        /* Estimate optimal kick-x */
        double receiverETA = 0.0;
        try {
            receiverETA = RobotMovement.calcETA(receiver.getDir(), receiver.getVel(),
                    receivingPos, receiver.getPos());
        } catch (NullPointerException e) {
            System.err.println(receiver);
            System.err.println(receivingPos);
        }
        double ballDist = passingPos.sub(receivingPos).mag();
        double s = BallMovement.calcKickVel(ballDist, receiverETA);
        s = Math.max(MIN_KICK_VEL, Math.min(s, MAX_KICK_VEL));
        double ballETA = BallMovement.calcETA(s, ballDist);

        /* Estimate optimal kick-z */
        double z = 0.0;
        if (robotSnaps != null) {
            for (RobotSnapshot robotSnap : robotSnaps) {
                Vec2D robotPos = robotSnap.getPos();
                if (robotPos.sub(receivingPos).mag() + robotPos.sub(passingPos).mag()
                        - receivingPos.sub(passingPos).mag() < KICK_Z_THRESHOLD) {
                    z = ballETA / 2.0 * GRAVITY * KICK_Z_FACTOR;
                    break;
                }
            }
        }

        return new Pair<>(new Vec2D(s, z),
                receiverETA * timeWeight(receiver.getPos().sub(receivingPos).mag()) < ballETA);
    }

}
