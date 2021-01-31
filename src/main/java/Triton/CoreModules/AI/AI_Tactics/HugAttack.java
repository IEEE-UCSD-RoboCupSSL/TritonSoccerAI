package Triton.CoreModules.AI.AI_Tactics;

import Triton.CoreModules.AI.AI_Skills.CoordinatedPass;
import Triton.CoreModules.AI.AI_Skills.PassStates;
import Triton.CoreModules.AI.Estimators.Estimator;
import Triton.CoreModules.AI.Estimators.PassEstimator;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;

public class HugAttack extends Tactics {

    protected Ally passer, receiver;
    protected Robot holder;

    public HugAttack(RobotList<Ally> allies, Ally keeper, RobotList<Foe> foes,
                     Ball ball, Estimator estimator, PassEstimator passEstimator) {
        super(allies, keeper, foes, ball, estimator, passEstimator);
    }


    private void b4return() {
        CoordinatedPass.setPending();
        passer = null;
        receiver = null;
    }

    @Override
    /* Assumes ball is under our control
     * */
    public boolean exec() {
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
                receiver = passEstimator.getOptimalReceiver();
            }

            /* pass & receive */
            PassStates pState;
            pState = CoordinatedPass.basicPass(passer, receiver, ball, estimator, passEstimator);
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
