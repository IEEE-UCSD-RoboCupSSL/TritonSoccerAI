package Triton.CoreModules.AI.AI_Tactics;

import Triton.CoreModules.AI.AI_Skills.CoordinatedPass;
import Triton.CoreModules.AI.AI_Skills.PassStates;
import Triton.CoreModules.AI.Estimators.Estimator;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;

public class HugAttack extends Tactics {

    protected Ally passer, receiver;
    protected Robot holder;

    private void b4return() {
        CoordinatedPass.setPending();
        passer = null;
        receiver = null;
    }

    @Override
    /* Assumes ball is under our control
     * */
    public boolean exec(RobotList<Ally> allies, RobotList<Foe> foes, Ball ball, Estimator estimator) {
        while(true) {
            if (!estimator.isBallUnderOurCtrl()) {
                b4return();
                return false;
            }

            if (CoordinatedPass.getPassState() == PassStates.PENDING) {
                /* determine passer & receiver */
                holder = estimator.getBallHolder();
                if (holder instanceof Ally) {
                    passer = (Ally) holder;
                } else {
                    b4return();
                    return false;
                }
                receiver = estimator.getOptimalReceiver();
            }

            /* pass & receive */
            PassStates pState;
            pState = CoordinatedPass.basicPass(passer, receiver, ball, estimator);
            if (pState == PassStates.FAILED) {
                b4return();
                return false;
            }
            if(pState == PassStates.RECEIVE_SUCCESS) {
                b4return();
                return true;
            }

            /* delegate remainder bots to hug opponent robots */
            if (pState == PassStates.PASSED) {
                // include passer in delegation
            }

            // .......



            // add delay to prevent starving other threads
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
