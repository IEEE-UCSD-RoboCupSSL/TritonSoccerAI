package Triton.CoreModules.AI.Estimators;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Coordinates.Vec2D;

public class PassEstimatorMock extends PassEstimator {
    private Vec2D passingPos;
    private Vec2D receivingPos;
    private Ally optimalReceiver;
    private boolean isGoodTimeToPass;
    private double ballArrivalETA;

    public PassEstimatorMock() {
        super(null, null, null, null);
        passingPos = new Vec2D(0, 0);
        receivingPos = new Vec2D(0, 0);
        optimalReceiver = null;
    }

    public void setPassingPos(Vec2D passingPos) {
        this.passingPos = passingPos;
    }

    public void setReceivingPos(Vec2D receivingPos) {
        this.receivingPos = receivingPos;
    }

    public void setOptimalReceiver(Ally optimalReceiver) {
        this.optimalReceiver = optimalReceiver;
    }

    public void setGoodTimeToPass(boolean goodTimeToPass) {
        isGoodTimeToPass = goodTimeToPass;
    }

    public void setBallArrivalETA(double ballArrivalETA) {
        this.ballArrivalETA = ballArrivalETA;
    }

    /* Estimates for Coordinated Passing */
    @Override
    public Vec2D getOptimalPassingPos(Ally passer) {
        return passingPos;
    }

    @Override
    public Ally getOptimalReceiver() {
        return optimalReceiver;
    }

    @Override
    public Vec2D getOptimalReceivingPos(Ally receiver) {
        return receivingPos;
    }

    /* return true if slack time > 0 */
    @Override
    public boolean isGoodTimeToPass() {
        return isGoodTimeToPass;
    }

    @Override
    public double getBallArrivalETA() {
        return ballArrivalETA;
    }

}
