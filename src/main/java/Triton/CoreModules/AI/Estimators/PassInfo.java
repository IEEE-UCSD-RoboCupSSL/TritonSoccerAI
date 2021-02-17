package Triton.CoreModules.AI.Estimators;

import Triton.CoreModules.AI.Estimators.TimeEstimator.BallMovement;
import Triton.CoreModules.AI.Estimators.TimeEstimator.RobotMovement;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Matrix.Vec2D;
import org.javatuples.Pair;

public class PassInfo {

    private final RobotList<Ally> allies;
    private final RobotList<Foe> foes;
    private final Ball ball;

    private static final double DIST_MEAN = 500.0;
    private static final double DIST_RANGE = 500.0;
    private static final double WEIGHT_MIN = 1.0;
    private static final double WEIGHT_MAX = 2.2;

    private static final double MAX_KICK_VEL = 4.0;
    private static final double MIN_KICK_VEL = 1.0;

    private Ally passer;
    private Ally receiver;
    private Vec2D passingPos;
    private Vec2D receivingPos;
    private double prob;

    public PassInfo(RobotList<Ally> allies, RobotList<Foe> foes, Ball ball) {
        this.allies = allies;
        this.foes = foes;
        this.ball = ball;
    }

    public void setInfo(int passerID, int receiverID,
                        Vec2D passingPos, Vec2D receivingPos, double prob) {
        this.passer = allies.get(passerID);
        this.receiver = allies.get(receiverID);
        this.passingPos = passingPos;
        this.receivingPos = receivingPos;
        this.prob = prob;
    }

    public double getMaxProb() {
        return prob;
    }

    public Vec2D getOptimalPassingPos(Ally passer) {
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

    public Pair<Double, Boolean> getPassDecision() {
        double receiverETA = RobotMovement.calcETA(receiver.getDir(), receiver.getVel(),
                receivingPos, receiver.getPos());
        double ballDist = passingPos.sub(receivingPos).mag();
        double s = BallMovement.calcKickVel(ballDist, receiverETA);
        s = Math.max(MIN_KICK_VEL, Math.min(s, MAX_KICK_VEL));
        double ballETA = BallMovement.calcETA(s, ballDist);
        return new Pair<>(s, receiverETA * timeWeight(receiver.getPos().sub(receivingPos).mag()) < ballETA);
    }

}
