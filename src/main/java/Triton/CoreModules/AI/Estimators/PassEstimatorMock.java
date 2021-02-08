package Triton.CoreModules.AI.Estimators;

import Triton.CoreModules.Robot.Ally;
import Triton.Misc.Math.Matrix.Vec2D;

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

    /* Estimates for Coordinated Passing */
    @Override
    public Vec2D getOptimalPassingPos(Ally passer) {
        return passingPos;
    }

    @Override
    public Ally getOptimalReceiver() {
        return optimalReceiver;
    }

    public void setOptimalReceiver(Ally optimalReceiver) {
        this.optimalReceiver = optimalReceiver;
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

    public void setGoodTimeToPass(boolean goodTimeToPass) {
        isGoodTimeToPass = goodTimeToPass;
    }

    @Override
    public double getBallArrivalETA() {
        return ballArrivalETA;
    }

    public void setBallArrivalETA(double ballArrivalETA) {
        this.ballArrivalETA = ballArrivalETA;
    }

}
