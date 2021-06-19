package Triton.CoreModules.AI.AI_Skills;


import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.PassInfo;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import org.javatuples.Pair;

import static Triton.Misc.Math.Coordinates.PerspectiveConverter.normAng;

public class DEPRECATED_CoordinatedPass extends Skills {

    private static Vec2D receivePos = null;
    private static final double GET_BALL_INTERVAL = 100;

    private static PassState currState = PassState.PENDING;

    private static long t0, t1;

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
                    t0 = System.currentTimeMillis();
                    currState = PassState.PASSER_HOLDS_BALL;
                } else {
                    currState = PassState.FAILED;
                }
            }
            case PASSER_HOLDS_BALL -> {
                if (!passer.isHoldingBall()) {
                    currState = PassState.FAILED;
                }

                if(System.currentTimeMillis() - t0 > 100) { // give dribbler time to drib
                    currState = PassState.PASSER_IN_POSITION;
                } else {
                    passer.stop();
                }
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
                if (DEPRECATED_CoordinatedPass.receivePos != null) {
                    receivePos = DEPRECATED_CoordinatedPass.receivePos;
                } else {
                    receivePos = DEPRECATED_CoordinatedPass.receivePos = passInfo.getOptimalReceivingPos();
                }
                double passAngle = receivePos.sub(passPos).toPlayerAngle();
                double receiveAngle = passPos.sub(receivePos).toPlayerAngle();
//                passer.rotateTo(passAngle);
                passer.dribRotate(ball, passAngle);
                if (receiver.isDirAimed(receiveAngle) && receiver.isPosArrived(receivePos)) {
                    currState = PassState.RECEIVER_IN_POSITION;
                } else {
                    receiver.strafeTo(receivePos, receiveAngle);
                    Pair<Double, Boolean> decision = passInfo.getKickDecision();
                    if (decision.getValue1() && passer.isDirAimed(passAngle)) {
                        passer.kick(new Vec2D(decision.getValue0(), 0));

                        t1 = System.currentTimeMillis();
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
                    Pair<Double, Boolean> decision = passInfo.getKickDecision();
                    passer.kick(new Vec2D(decision.getValue0(), 0));

                    t1 = System.currentTimeMillis();
                    currState = PassState.PASSED;
                } else {
                    receiver.curveTo(receivePos, receiveAngle);
//                    passer.rotateTo(passAngle);
                    passer.dribRotate(ball, passAngle);
                }
            }
            case PASSED -> {
                long timeGap = System.currentTimeMillis() - t1;
                System.out.println("timegap:" + timeGap);
                if(timeGap > 1000) {
                    currState = PassState.FAILED;
                }


                // fix receive pos
                Vec2D __receivePos;
                if (receivePos != null) {
                    __receivePos = receivePos;
                } else {
                    __receivePos = receivePos = passInfo.getOptimalReceivingPos();
                }

                // in this state, passer may be null
                if (receiver.isHoldingBall()) {
                    currState = PassState.RECEIVE_SUCCESS;
                } else {
                    if (basicEstimator.getBallHolder() instanceof Foe) {
                        currState = PassState.FAILED;
                    } else {
                        Vec2D ballVel = ball.getVel();
                        double faceDir;
                        faceDir = normAng(180 + ballVel.toPlayerAngle());

                        if(ball.getPos().sub(__receivePos).mag() < 1000) {
                            receiver.dynamicIntercept(ball, faceDir);
                        } else {
                            receiver.curveTo(receivePos);
                        }

                    }
                }
                receivePos = null; // unfix
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
