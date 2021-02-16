package Triton.CoreModules.AI.AI_Tactics;

import Triton.CoreModules.AI.AI_Skills.CoordinatedPass;
import Triton.CoreModules.AI.AI_Skills.Dodging;
import Triton.CoreModules.AI.AI_Skills.PassState;
import Triton.CoreModules.AI.AI_Skills.Swarm;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.GapFinder;
import Triton.CoreModules.AI.Estimators.PassEstimator;
import Triton.CoreModules.AI.Estimators.PassFinder;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Matrix.Vec2D;

import java.util.ArrayList;

public class AttackPlanA extends Tactics {

    protected Ally passer, receiver;
    protected Robot holder;
    protected final BasicEstimator basicEstimator;
    protected final PassEstimator passEstimator;
    private GapFinder gapFinder;
    private PassFinder passFinder;
    private Dodging dodging;
    private RobotList<Ally> restFielders = null;
    final private double interAllyClearance = 600; // mm

    public AttackPlanA(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes,
                       Ball ball, GapFinder gapFinder, PassFinder passFinder) {
        super(fielders, keeper, foes, ball);

        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        passEstimator = new PassEstimator(fielders, keeper, foes, ball);

        this.gapFinder = gapFinder;
        this.passFinder = passFinder;
        this.dodging = new Dodging(fielders, foes, ball, basicEstimator);
    }

    @Override
    public boolean exec() {
        // should be invoked within a loop
        // invoking contract: ball is under our control
        holder = basicEstimator.getBallHolder();
        if (!(holder instanceof Ally)) {
            return false;
        }


        if(passEstimator.isReadyToStartPass()) {


        } else {
            Vec2D holdBallPos = ((Ally) holder).HoldBallPos();
            // System.out.println(holdBallPos);
            if(holdBallPos != null) {
                dodging.dodge((Ally) holder, holdBallPos);
            }
            restOfAllyForwardFillGap();

        }


        return true;
    }

    private void restOfAllyForwardFillGap() {

        restFielders = (RobotList<Ally>) fielders.clone();
        restFielders.remove((Ally)holder);
        if(restFielders == null) {
            return;
        }

        Vec2D ballPos = ball.getPos();

        ArrayList<Vec2D> gapPos = gapFinder.getTopNMaxPosWithClearance(restFielders.size(), interAllyClearance);
        if(gapPos != null) {
            ArrayList<Double> gapPosDir = new ArrayList<>();
            for(Vec2D pos : gapPos) {
                gapPosDir.add(ballPos.sub(pos).toPlayerAngle());
            }

            new Swarm(restFielders).groupTo(gapPos, gapPosDir, ballPos); // ballPos used as priorityAnchor
        }
    }


    private boolean passReceive() {


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
