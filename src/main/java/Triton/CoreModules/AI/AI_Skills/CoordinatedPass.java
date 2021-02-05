package Triton.CoreModules.AI.AI_Skills;


import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.PassEstimator;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.Misc.Coordinates.Vec2D;

public class CoordinatedPass extends Skills {

    private static PassState currState = PassState.PENDING;

    public static PassState getPassState() {
        return currState;
    }

    public static void setPending() {
        currState = PassState.PENDING;
    }

    public static PassState basicPass(Ally passer, Ally receiver, Ball ball,
                                      BasicEstimator basicEstimator, PassEstimator passEstimator) {


        //To-do: add if(timeout) ... -> FAILED

        /* State Machine */
        switch (currState) {
            case PENDING -> {
                if (passer.isHoldingBall()) {
                    currState = PassState.PASSER_HOLDS_BALL;
                } else {
                    return PassState.PENDING;
                }
            }
            case PASSER_HOLDS_BALL -> {
                if (!passer.isHoldingBall()) {
                    currState = PassState.FAILED;
                }
                Vec2D passLoc = passEstimator.getOptimalPassingPos(passer); // getOptimalxxxx methods might be time-consuming
                Vec2D receivePos = passEstimator.getOptimalReceivingPos(receiver);
                if (passer.isPosArrived(passLoc)) {
                    currState = PassState.PASSER_IN_POSITION;
                } else {
                    double passAngle = receivePos.sub(passLoc).toPlayerAngle();
                    double receiveAngle = passLoc.sub(receivePos).toPlayerAngle();
                    passer.strafeTo(passLoc, passAngle);
                    receiver.curveTo(receivePos, receiveAngle);
                }
            }
            case PASSER_IN_POSITION -> {
                if (!passer.isHoldingBall()) {
                    currState = PassState.FAILED;
                }
                Vec2D passPos = passEstimator.getOptimalPassingPos(passer);
                Vec2D receivePos = passEstimator.getOptimalReceivingPos(receiver);
                double passAngle = receivePos.sub(passPos).toPlayerAngle();
                double receiveAngle = passPos.sub(receivePos).toPlayerAngle();
                passer.rotateTo(passAngle);
                if (receiver.isDirAimed(receiveAngle) && receiver.isPosArrived(receivePos)) {
                    currState = PassState.RECEIVER_IN_POSITION;
                } else {
                    receiver.curveTo(receivePos, receiveAngle);
                    if (passEstimator.isGoodTimeToPass() && passer.isDirAimed(passAngle)) {
                        passer.passBall(receivePos, passEstimator.getBallArrivalETA());
                        currState = PassState.PASSED;
                    }
                }
            }
            case RECEIVER_IN_POSITION -> {
                if (!passer.isHoldingBall()) {
                    currState = PassState.FAILED;
                }
                Vec2D passPos = passEstimator.getOptimalPassingPos(passer);
                Vec2D receivePos = passEstimator.getOptimalReceivingPos(receiver);
                double passAngle = receivePos.sub(passPos).toPlayerAngle();
//                double receiveAngle = passPos.sub(receivePos).toPlayerAngle();
                passer.rotateTo(passAngle);
                if (passEstimator.isGoodTimeToPass() && passer.isDirAimed(passAngle)) {
                    passer.passBall(receivePos, passEstimator.getBallArrivalETA());
                    currState = PassState.PASSED;
                } else {
                    receiver.receive(ball, receivePos);
                }
            }
            case PASSED -> {
                Vec2D receivePos = passEstimator.getOptimalReceivingPos(receiver);

                // in this state, passer may be null
                if (receiver.isHoldingBall()) {
                    currState = PassState.RECEIVE_SUCCESS;
                } else {
                    if (basicEstimator.getBallHolder() instanceof Foe) {
                        currState = PassState.FAILED;
                    } else {
                        receiver.receive(ball, receivePos);
                    }
                }
            }
            case RECEIVE_SUCCESS -> {
                // better to set pending outside for readability
                // setPending();
                return PassState.RECEIVE_SUCCESS;
            }
            case FAILED -> {
                // setPending();
                return PassState.FAILED;
            }
        }

        return currState;
    }


    public static PassState passWithPasserDodging() {
        /* getPos Later */


        return currState;
    }


}
