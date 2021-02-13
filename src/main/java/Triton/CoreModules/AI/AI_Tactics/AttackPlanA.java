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

public class AttackPlanA extends Tactics {

    protected Ally passer, receiver;
    protected Robot holder;

    public AttackPlanA(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes,
                       Ball ball, BasicEstimator basicEstimator, PassEstimator passEstimator) {
        super(fielders, keeper, foes, ball, basicEstimator, passEstimator);
    }

    @Override
    public boolean exec() {
        // should be invoked within a loop
        // invoking contract: ball is under our control


        holder = basicEstimator.getBallHolder();
        if (!(holder instanceof Ally)) {
            return false;
        }

        // .......


        /*** pass & receive ***/
        if (CoordinatedPass.getPassState() == PassState.PENDING) {
            /* determine which fielders to be the passer & receiver */
            passer = (Ally) holder;
            receiver = passEstimator.getOptimalReceiver();
        }
        PassState passState = CoordinatedPass.basicPass(passer, receiver, ball, basicEstimator, passEstimator);
        System.out.println(passState);
        switch (passState) {
            case PASSED -> {
                if (passer != null) {

                }
            }
            case RECEIVE_SUCCESS -> {

            }
            case FAILED -> {
                return false;
            }
        }

        /*** delegate remaining bots to hug opponent robots ***/


        return true;
    }
}
