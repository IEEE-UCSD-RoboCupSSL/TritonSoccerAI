package Triton.CoreModules.AI.AI_Skills;


import Triton.CoreModules.AI.Estimators.Estimator;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.Misc.Coordinates.Vec2D;

public class CoordinatedPass extends Skills {

    private static PassStates currState = PassStates.PENDING;

    public static PassStates getPassState() {
        return currState;
    }

    public static void setPending() {
        currState = PassStates.PENDING;
    }

    public static PassStates basicPass(Ally passer, Ally receiver, Estimator estimator) {


        //To-do: add if(timeout) ... -> FAILED

        /* State Machine */
        switch (currState) {
            case PENDING -> {
                if(passer.isHoldingBall()) {
                    currState = PassStates.PASSER_HOLDS_BALL;
                }
            }
            case PASSER_HOLDS_BALL -> {
                if(!passer.isHoldingBall()) {
                    currState = PassStates.FAILED;
                }
                Vec2D passLoc = estimator.getOptimalPassingLoc(passer); // getOptimalxxxx methods might be time-consuming
                Vec2D receiveLoc = estimator.getOptimalReceivingLoc(receiver);
                if(passer.isLocArrived(passLoc)) {
                    currState = PassStates.PASSER_IN_POSITION;
                }
                else {
                    double passAngle = receiveLoc.sub(passLoc).getAngle(); // To-do: check math
                    double receiveAngle = passLoc.sub(receiveLoc).getAngle(); // To-do: check math
                    passer.strafeTo(passLoc, passAngle);
                    receiver.sprintToAngle(receiveLoc, receiveAngle);
                }
            }
            case PASSER_IN_POSITION -> {
                if(!passer.isHoldingBall()) {
                    currState = PassStates.FAILED;
                }
                Vec2D passLoc = estimator.getOptimalPassingLoc(passer);
                Vec2D receiveLoc = estimator.getOptimalReceivingLoc(receiver);
                double receiveAngle = passLoc.sub(receiveLoc).getAngle(); // To-do: check math
                if(receiver.isAngleAimed(receiveAngle) && receiver.isLocArrived(receiveLoc)) {
                    currState = PassStates.RECEIVER_IN_POSITION;
                }
                else {
                    receiver.sprintToAngle(receiveLoc, receiveAngle);
                    if(estimator.isGoodTimeToPass()) {
                        passer.pass(receiveLoc, estimator.getBallArrivalETA());
                        currState = PassStates.PASSED;
                    }
                }
            }
            case RECEIVER_IN_POSITION -> {
                if(!passer.isHoldingBall()) {
                    currState = PassStates.FAILED;
                }
                Vec2D passLoc = estimator.getOptimalPassingLoc(passer);
                Vec2D receiveLoc = estimator.getOptimalReceivingLoc(receiver);
                double receiveAngle = passLoc.sub(receiveLoc).getAngle(); // To-do: check math
                if(estimator.isGoodTimeToPass()) {
                    passer.pass(receiveLoc, estimator.getBallArrivalETA());
                    currState = PassStates.PASSED;
                }
                else {
                    receiver.sprintToAngle(receiveLoc, receiveAngle);
                }
            }
            case PASSED -> {
                // in this state, passer will be null
                if(receiver.isHoldingBall()) {
                    currState = PassStates.RECEIVE_SUCCESS;
                }
                else {
                    if(estimator.getBallHolder() instanceof Foe) {
                        currState = PassStates.FAILED;
                    }
                    else {
                        receiver.intercept();
                    }
                }
            }
            case RECEIVE_SUCCESS -> {
                // better to set pending outside for readability
                // setPending();
                return PassStates.RECEIVE_SUCCESS;
            }
            case FAILED -> {
                // setPending();
                return PassStates.FAILED;
            }
        }


        return currState;
    }



    public static PassStates passWithPasserDodging() {
        /* To-Do Later */


        return currState;
    }


}
