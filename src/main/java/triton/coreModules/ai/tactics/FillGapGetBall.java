package triton.coreModules.ai.tactics;

import triton.config.Config;
import triton.coreModules.ai.skills.Swarm;
import triton.coreModules.ai.estimators.BasicEstimator;
import triton.coreModules.ai.estimators.AttackSupportMapModule;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.RobotList;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.ArrayList;

public class FillGapGetBall extends Tactics {

    final private double interAllyClearance = 600; // mm
    protected final BasicEstimator basicEstimator;
    // private int state;
    private Ally nearestFielder = null;
    private RobotList<Ally> restFielders = null;
    private AttackSupportMapModule atkSupportMap;
    private final Config config;

    public FillGapGetBall(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball, AttackSupportMapModule atkSupportMap, Config config) {
        super(fielders, keeper, foes, ball);
        this.config = config;
        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        this.atkSupportMap = atkSupportMap;
        //state = 0;
    }

    public AttackSupportMapModule getatkSupportMap() {
        return atkSupportMap;
    }

    @Override
    public boolean exec() {
        // should be invoked within a loop
        // invoking contract: no robot is holding the ball

        /* find nearest ally to the ball */
        nearestFielder = basicEstimator.getNearestFielderToBall();

        restFielders = (RobotList<Ally>) fielders.clone();
        if(nearestFielder != null) {
            restFielders.remove(nearestFielder);
        }

        if(nearestFielder == null || restFielders == null) {
            return false;
        }

        Vec2D ballPos = ball.getPos();

        ArrayList<Vec2D> gapPos = atkSupportMap.getTopNMaxPosWithClearance(restFielders.size(), interAllyClearance);
        if(gapPos != null) {
            ArrayList<Double> gapPosDir = new ArrayList<>();
            for(Vec2D pos : gapPos) {
                gapPosDir.add(ballPos.sub(pos).toPlayerAngle());
            }

            new Swarm(restFielders, config).groupTo(gapPos, gapPosDir, ballPos); // ballPos used as priorityAnchor
        }

        if(basicEstimator.getBallHolder() instanceof Ally) {
            return true;
        }
        nearestFielder.getBall(ball);

        return false;
    }
}


// state machine method
//        if(basicEstimator.isBallUnderOurCtrl()) {
//            state = 0;
//            return true;
//        }

//        switch (state) {
//            case 0 -> {
//                /* find nearest ally to the ball */
//                for (Ally fielder : fielders) {
//                    if (nearestFielder == null ||
//                            fielder.getPos().sub(ball.getPos()).mag() < nearestFielder.getPos().sub(ball.getPos()).mag()) {
//                        nearestFielder = fielder;
//                    }
//                }
//                restFielders = (RobotList<Ally>) fielders.clone();
//                if(nearestFielder != null) {
//                    restFielders.remove(nearestFielder);
//                    nearestFielder.getBall(ball);
//                }
//
//                state++;
//            }
//            case 1 -> {
//                if(nearestFielder == null || restFielders == null) {
//                    break;
//                }
//                nearestFielder.getBall(ball);
//                ArrayList<Vec2D> gapPos = atkSupportMap.getTopNMaxPosWithClearance(restFielders.size(), interAllyClearance);
//                if(gapPos != null) {
//                    new Swarm(restFielders).groupTo(gapPos);
//                }
//            }
//
//            default -> {
//                state = 0;
//            }
//        }