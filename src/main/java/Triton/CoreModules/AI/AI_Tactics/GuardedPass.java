package Triton.CoreModules.AI.AI_Tactics;

import Triton.CoreModules.AI.AI_Skills.CoordinatedPass;
import Triton.CoreModules.AI.AI_Skills.PassStates;
import Triton.CoreModules.AI.Estimators.Estimator;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;

public class GuardedPass extends Tactics {

    protected Ally passer, receiver;
    protected Robot holder;

    @Override
    /* Assumes ball is under our control
     * */
    public boolean exec(RobotList<Ally> allies, RobotList<Foe> foes, Ball ball, Estimator estimator) {
        while(true) {
            if (!estimator.isBallUnderOurCtrl()) {
                return false;
            }

            if (CoordinatedPass.getPassState() == PassStates.PENDING) {
                /* determine passer & receiver */
                holder = estimator.getBallHolder();
                if (holder instanceof Ally) {
                    passer = (Ally) holder;
                } else {
                    passer = null;
                }
                receiver = estimator.getOptimalReceiver();
            }

            /* delegate remainder bots to guard opponent robots */
            if (passer == null) {
                // include passer in delegation
            }

            // .......

            /* pass & receive */
            PassStates pState;
            pState = CoordinatedPass.basicPass(passer, receiver, estimator);
            if (pState == PassStates.FAILED) {
                CoordinatedPass.setPending();
                return false;
            }
            if(pState == PassStates.RECEIVE_SUCCESS) {
                CoordinatedPass.setPending();
                return true;
            }


            // add delay to prevent starving other threads
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
