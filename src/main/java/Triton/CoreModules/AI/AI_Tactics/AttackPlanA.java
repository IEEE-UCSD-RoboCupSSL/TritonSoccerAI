package Triton.CoreModules.AI.AI_Tactics;

import Triton.CoreModules.AI.AI_Skills.CoordinatedPass;
import Triton.CoreModules.AI.AI_Skills.Dodging;
import Triton.CoreModules.AI.AI_Skills.PassState;
import Triton.CoreModules.AI.AI_Skills.Swarm;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.GapFinder;
import Triton.CoreModules.AI.Estimators.PassInfo;
import Triton.CoreModules.AI.Estimators.PassFinder;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Matrix.Vec2D;

import java.util.ArrayList;

public class AttackPlanA extends Tactics {

    private static final double SHOOT_THRESHOLD = 0.6;

    protected Ally passer, receiver;
    protected Robot holder;
    protected final BasicEstimator basicEstimator;
    protected final PassInfo passInfo;
    private GapFinder gapFinder;
    private PassFinder passFinder;
    private Dodging dodging;
    private RobotList<Ally> restFielders = null;
    final private double interAllyClearance = 600; // mm

    public AttackPlanA(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes,
                       Ball ball, GapFinder gapFinder, PassFinder passFinder) {
        super(fielders, keeper, foes, ball);

        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        passInfo = new PassInfo(fielders, foes, ball);

        this.gapFinder = gapFinder;
        this.passFinder = passFinder;
        this.dodging = new Dodging(fielders, foes, ball, basicEstimator);
    }


    private boolean isReadyToShoot() {
        if (holder == null) return false;
        if (passFinder == null) return false;
        double[][] gProbs = passFinder.getGProb();
        if (gProbs == null) return false;

        int[] holderIdx = passFinder.getIdxFromPos(holder.getPos());
        double gProb = gProbs[holderIdx[0]][holderIdx[1]];
        return gProb > SHOOT_THRESHOLD;
    }


    @Override
    public boolean exec() {
        // should be invoked within a loop
        // invoking contract: ball is under our control
        holder = basicEstimator.getBallHolder();
        if (!(holder instanceof Ally)) {
            return false;
        }

        /*if(isReadyToShoot()) {
        }*/

        /*if (readyToPass) {
            Vec2D passPos = result.getValue0();
            Integer receiver = result.getValue2();
            launchPassReceive();

            System.out.println("receiver: " + result.getValue2());

        } else {
            Vec2D holdBallPos = ((Ally) holder).HoldBallPos();
            if(holdBallPos != null) {
                dodging.dodge((Ally) holder, holdBallPos);
            }

            restOfAllyFillGap(ball.getPos());
        }*/


        return true;
    }

    private void restOfAllyFillGap(Vec2D priorityAnchor) {

        restFielders = (RobotList<Ally>) fielders.clone();
        restFielders.remove((Ally)holder);
        if(restFielders == null) {
            return;
        }


        ArrayList<Vec2D> gapPos = gapFinder.getTopNMaxPosWithClearance(restFielders.size(), interAllyClearance);
        if(gapPos != null) {
            ArrayList<Double> gapPosDir = new ArrayList<>();
            for(Vec2D pos : gapPos) {
                gapPosDir.add(ball.getPos().sub(pos).toPlayerAngle());
            }

            new Swarm(restFielders).groupTo(gapPos, gapPosDir, priorityAnchor); // ballPos used as priorityAnchor
        }
    }


    private boolean launchPassReceive() {

        /*** pass & receive ***/
        if (CoordinatedPass.getPassState() == PassState.PENDING) {
            /* determine which fielders to be the passer & receiver */
            passer = (Ally) holder;
            receiver = passInfo.getOptimalReceiver();
        }
        PassState passState = CoordinatedPass.basicPass(passer, receiver, ball, basicEstimator, passInfo);
        // System.out.println(passState);
        switch (passState) {
            case PASSED -> {
                if (passer != null) {

                }
            }
            case RECEIVE_SUCCESS -> {
                return true;
            }
            case FAILED -> {
                return false;
            }
        }

        /*** delegate remaining bots to hug opponent robots ***/
        restOfAllyFillGap(new Vec2D(0, 4500));


        return true;
    }
}
