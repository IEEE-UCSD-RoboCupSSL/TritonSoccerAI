package Triton.CoreModules.AI.AI_Skills;


import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.PassInfo;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.Misc.Math.Matrix.Vec2D;
import org.javatuples.Pair;

import static Triton.Misc.Math.Coordinates.PerspectiveConverter.normAng;

public class CoordinatedPass extends Skills {

    private static Vec2D fixReceivePos = null;
    private static final double GET_BALL_INTERVAL = 100;

    private static PassState currState = PassState.PENDING;

    public static PassState getPassState() {
        return currState;
    }

    private static void resetPassStateToPending() {
        currState = PassState.PENDING;
    }

    public static PassState basicPass(Ally passer, Ally receiver, Ball ball,
                                      BasicEstimator basicEstimator, PassInfo passInfo) {

        //To-do: add if(timeout) ... -> FAILED

        /* State Machine */
        switch (currState) {
            case PENDING -> {
                if (!(passer == null) && passer.isHoldingBall()) {
                    currState = PassState.PASSER_HOLDS_BALL;
                } else {
                    currState = PassState.PENDING;
                }
            }
            case PASSER_HOLDS_BALL -> {
                if (!passer.isHoldingBall()) {
                    currState = PassState.FAILED;
                }
                currState = PassState.PASSER_IN_POSITION;
                /*Vec2D passLoc = passInfo.getOptimalPassingPos(passer);
                Vec2D receivePos = passInfo.getOptimalReceivingPos();
                if (passer.isPosArrived(passLoc)) {
                    currState = PassState.PASSER_IN_POSITION;
                } else {
                    double passAngle = receivePos.sub(passLoc).toPlayerAngle();
                    double receiveAngle = passLoc.sub(receivePos).toPlayerAngle();
                    passer.strafeTo(passLoc, passAngle);
                    receiver.curveTo(receivePos, receiveAngle);
                }*/
            }
            case PASSER_IN_POSITION -> {
                if (!passer.isHoldingBall()) {
                    currState = PassState.FAILED;
                }
                Vec2D passPos = passInfo.getOptimalPassingPos(passer);
                // fix receive pos
                Vec2D receivePos;
                if (fixReceivePos != null) {
                    receivePos = fixReceivePos;
                } else {
                    receivePos = fixReceivePos = passInfo.getOptimalReceivingPos();
                }
                double passAngle = receivePos.sub(passPos).toPlayerAngle();
                double receiveAngle = passPos.sub(receivePos).toPlayerAngle();
                passer.rotateTo(passAngle);
                if (receiver.isDirAimed(receiveAngle) && receiver.isPosArrived(receivePos)) {
                    currState = PassState.RECEIVER_IN_POSITION;
                } else {
                    receiver.strafeTo(receivePos, receiveAngle);
                    Pair<Double, Boolean> decision = passInfo.getPassDecision();
                    if (decision.getValue1() && passer.isDirAimed(passAngle)) {
                        passer.kick(new Vec2D(decision.getValue0(), 0));
                        currState = PassState.PASSED; // set next state directly to the PASSED state
                    }
                }
            }
            case RECEIVER_IN_POSITION -> {
                if (!passer.isHoldingBall()) {
                    currState = PassState.FAILED;
                }
                Vec2D passPos = passInfo.getOptimalPassingPos(passer);
                Vec2D receivePos = passInfo.getOptimalReceivingPos();
                double passAngle = receivePos.sub(passPos).toPlayerAngle();
                double receiveAngle = passPos.sub(receivePos).toPlayerAngle();
                if (true) { //if (passer.isDirAimed(passAngle)) {
                    Pair<Double, Boolean> decision = passInfo.getPassDecision();
                    passer.kick(new Vec2D(decision.getValue0(), 0));
                    currState = PassState.PASSED;
                } else {
                    receiver.strafeTo(receivePos, receiveAngle);
                    passer.rotateTo(passAngle);
                }
            }
            case PASSED -> {
                // fix receive pos
                Vec2D receivePos;
                if (fixReceivePos != null) {
                    receivePos = fixReceivePos;
                } else {
                    receivePos = fixReceivePos = passInfo.getOptimalReceivingPos();
                }

                // in this state, passer may be null
                if (receiver.isHoldingBall()) {
                    currState = PassState.RECEIVE_SUCCESS;
                } else {
                    if (basicEstimator.getBallHolder() instanceof Foe) {
                        currState = PassState.FAILED;
                    } else {
                        receiver.dynamicIntercept(ball, normAng(180 + ball.getVel().toPlayerAngle()));
                    }
                }
                fixReceivePos = null; // unfix
            }
            case RECEIVE_SUCCESS, FAILED -> {
                currState = PassState.PENDING;
            }
        }

        return currState;
    }


    public static PassState passWithPasserDodging() {
        /* getPos Later */


        return currState;
    }


}
