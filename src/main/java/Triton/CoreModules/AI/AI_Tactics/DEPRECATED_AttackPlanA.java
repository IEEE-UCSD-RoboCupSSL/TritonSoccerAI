package Triton.CoreModules.AI.AI_Tactics;

import Triton.Config.Config;
import Triton.CoreModules.AI.AI_Skills.*;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.AttackSupportMapModule;
import Triton.CoreModules.AI.Estimators.PassInfo;
import Triton.CoreModules.AI.Estimators.PassProbMapModule;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Geometry.Line2D;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

import java.util.ArrayList;

import static Triton.Config.OldConfigs.ObjectConfig.MAX_KICK_VEL;
import static Triton.Misc.Math.Coordinates.PerspectiveConverter.normAng;

public class DEPRECATED_AttackPlanA extends Tactics {

    private static final double SHOOT_THRESHOLD = 0.6;
    private static final double PASS_THRESHOLD = 0.45;

    protected Ally passer, receiver;
    protected Robot holder;
    protected final BasicEstimator basicEstimator;
    private PassInfo passInfo;
    private AttackSupportMapModule atkSupportMap;
    private PassProbMapModule passProbMap;
    private Dodging dodging;
    final private double interAllyClearance = 600; // mm
    private Config config;

    public DEPRECATED_AttackPlanA(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes,
                                  Ball ball, AttackSupportMapModule atkSupportMap, PassProbMapModule passProbMap, Config config) {
        super(fielders, keeper, foes, ball);
        this.config = config;

        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        passInfo = new PassInfo(fielders, foes, ball);

        this.atkSupportMap = atkSupportMap;
        this.passProbMap = passProbMap;
        this.dodging = new Dodging(fielders, foes, ball, basicEstimator);
    }


    private boolean isReadyToShoot() {
        if (holder == null) return false;
        if (passProbMap == null) return false;
        double[][] gProbs = passProbMap.getGProb();
        if (gProbs == null) return false;

        int[] holderIdx = passProbMap.getIdxFromPos(holder.getPos());
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

        if(isReadyToShoot() && holder instanceof Ally) {
            shootingProcedure((Ally) holder);
        }

        passInfo = passProbMap.evalPass();
        if(passInfo == null) {
            return false;
        }

        if (passInfo.getMaxProb() > PASS_THRESHOLD) {
            int passRtnState;
            do {
                // add time limit
                passRtnState = launchPassReceive(passInfo);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }while(passRtnState == 0);
            launchPassReceive(passInfo); // run it one more iteration to reset state
            if(passRtnState == -1) {
                return false;
            }

        } else {
            Vec2D holdBallPos = ((Ally) holder).HoldBallPos();
            if(holdBallPos != null) {
                dodging.dodge((Ally) holder, holdBallPos);
            }
            RobotList<Ally> restFielders = (RobotList<Ally>) fielders.clone();
            restFielders.remove((Ally)holder);
            restOfAllyFillGap(restFielders, ball.getPos());
        }


        return true;
    }

    private void restOfAllyFillGap(RobotList<Ally> restFielders, Vec2D priorityAnchor) {
        if(restFielders == null) {
            return;
        }
        ArrayList<Vec2D> gapPos = atkSupportMap.getTopNMaxPosWithClearance(restFielders.size(), interAllyClearance);
        if(gapPos != null) {
            ArrayList<Double> gapPosDir = new ArrayList<>();
            for(Vec2D pos : gapPos) {
                gapPosDir.add(ball.getPos().sub(pos).toPlayerAngle());
            }

            new Swarm(restFielders, config).groupTo(gapPos, gapPosDir, priorityAnchor); // ballPos used as priorityAnchor
        }
    }


    private int launchPassReceive(PassInfo passInfo) {

        /*** pass & receive ***/
        if (DEPRECATED_CoordinatedPass.getPassState() == PassState.PENDING) {
            /* determine which fielders to be the passer & receiver */
            passer = (Ally) holder;
            receiver = passInfo.getOptimalReceiver();
        }
        PassState passState = DEPRECATED_CoordinatedPass.basicPass(passer, receiver, ball, basicEstimator, passInfo);
        System.out.println(passState);
        switch (passState) {
            case PASSED -> {
                if (passer != null) {
                    passer.stop(); // to-do
                }
            }
            case RECEIVE_SUCCESS -> {
                return 1;
            }
            case FAILED -> {
                return -1;
            }
        }

        /*** delegate remaining bots to hug opponent robots ***/

        RobotList<Ally> restFielders = (RobotList<Ally>) fielders.clone();
        restFielders.remove((Ally)holder);
        restFielders.remove(receiver);
        restOfAllyFillGap(restFielders, new Vec2D(0, 4500));


        return 0;
    }


    private void shootingProcedure(Ally shooter) {
        System.out.println("GOOD LUCK");
        long t0 = System.currentTimeMillis();
        while(System.currentTimeMillis() - t0 < 3000) {
            if  (!shooter.isHoldingBall()) {
                break;
            }
            else {
                adhocShooting(shooter);
            }
            RobotList<Ally> restFielders = (RobotList<Ally>) fielders.clone();
            restFielders.remove(shooter);
            //restOfAllyFillGap(restFielders, ball.getPos());
            ArrayList<Vec2D> sidePos = new ArrayList<>();
            sidePos.add(new Vec2D(2800, 500));
            sidePos.add(new Vec2D(2800, 0));
            sidePos.add(new Vec2D(-2800, 500));
            sidePos.add(new Vec2D(-2800, 0));
            new Swarm(restFielders, config).groupTo(sidePos);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void adhocShooting(Ally shooter) {
        Vec2D leftGoalPole = new Vec2D(-500, 4500);
        Vec2D rightGoalPole = new Vec2D(500, 4500);
        Vec2D shooterPos = shooter.getPos();
        double leftAngle = leftGoalPole.sub(shooterPos).toPlayerAngle();
        double rightAngle = rightGoalPole.sub(shooterPos).toPlayerAngle();
        double currAim = shooter.getDir();
        double leftOffset = normAng(currAim - leftAngle);
        double rightOffset = normAng(currAim - rightAngle);
        if(leftOffset < 0 && rightOffset > 0) {
            // aiming inside goal direction

            // check if good to shoot
            Line2D currAimLine = new Line2D(shooterPos.add(new Vec2D(currAim)).scale(100), shooterPos);
            Robot nearAimLineBot = basicEstimator.getNearestBotToLine(currAimLine);
            if(nearAimLineBot == null) return;
            if(nearAimLineBot.getPos().distToLine(currAimLine) < 150) {
                shooter.kick(new Vec2D(MAX_KICK_VEL, 0));
                return;
            }

            // scanning
            Vec2D scanPos = new Vec2D(-500, 4500);
            Vec2D optPos = null;
            double maxScore = 0;
            for(int i = 1; i < 10; i++) {
                Line2D shootingLine = new Line2D(scanPos, shooterPos);
                Robot nearestBot = basicEstimator.getNearestBotToLine(shootingLine);
                if(nearestBot == null) return;
                double score = nearestBot.getPos().distToLine(shootingLine);
                if(score > maxScore) {
                    maxScore = score;
                    optPos = scanPos;
                }
                scanPos = scanPos.add(new Vec2D(i * 100, 0));
            }

            double optAngle = optPos.sub(shooterPos).toPlayerAngle();
            shooter.rotateTo(optAngle);

        } else { // outside goal direction
            if(Math.abs(leftOffset) < Math.abs(rightOffset)) {
                shooter.rotateTo(normAng(leftAngle - 10));

            }else {
                shooter.rotateTo(normAng(rightAngle + 10));
            }
        }



    }

}
