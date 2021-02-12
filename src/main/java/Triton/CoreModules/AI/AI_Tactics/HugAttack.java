package Triton.CoreModules.AI.AI_Tactics;

import Triton.CoreModules.AI.AI_Skills.CoordinatedPass;
import Triton.CoreModules.AI.AI_Skills.PassState;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
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
                     Ball ball, BasicEstimator basicEstimator, PassEstimator passEstimator) {
        super(allies, keeper, foes, ball, basicEstimator, passEstimator);
    }

    @Override
    /* Assumes ball is under our control
     * */
    public boolean exec() {
        while (true) {
            if (!basicEstimator.isBallUnderOurCtrl()) {
                b4return();
                return false;
            }

            if (CoordinatedPass.getPassState() == PassState.PENDING) {
                /* determine passer & receiver */
                holder = basicEstimator.getBallHolder();
                if (holder instanceof Ally) {
                    passer = (Ally) holder;
                } else {
                    b4return();
                    return false;
                }
                receiver = passEstimator.getOptimalReceiver();
            }

            /* pass & receive */
            PassState pState;
            pState = CoordinatedPass.basicPass(passer, receiver, ball, basicEstimator, passEstimator);
            if (pState == PassState.FAILED) {
                b4return();
                return false;
            }
            if (pState == PassState.RECEIVE_SUCCESS) {
                b4return();
                return true;
            }

            /* delegate remainder bots to hug opponent robots */
            if (pState == PassState.PASSED) {
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

    private void b4return() {
        // CoordinatedPass.resetPassStateToPending();
        passer = null;
        receiver = null;
    }
}
