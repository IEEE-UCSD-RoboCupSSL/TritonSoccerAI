package Triton.CoreModules.AI.AI_Tactics;

import Triton.CoreModules.AI.AI_Skills.Swarm;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.GapFinder;
import Triton.CoreModules.AI.Estimators.PassEstimator;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Matrix.Vec2D;
import org.ejml.All;

import java.util.ArrayList;

public class FillGapGetBall extends Tactics {

    final private GapFinder gapFinder;
    final private double interAllyClearance = 300; // mm
    protected final BasicEstimator basicEstimator;
    private int state;
    private Ally nearestFielder = null;
    private RobotList<Ally> restFielders = null;

    public FillGapGetBall(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {
        super(fielders, keeper, foes, ball);
        gapFinder = new GapFinder(fielders, foes, ball);
        gapFinder.run();
        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        state = 0;
    }

    public GapFinder getGapFinder() {
        return gapFinder;
    }

    @Override
    public boolean exec() {
        // should be invoked within a loop
        // invoking contract: no robot is holding the ball

        /* find nearest ally to the ball */
        for (Ally fielder : fielders) {
            if (nearestFielder == null ||
                    fielder.getPos().sub(ball.getPos()).mag() < nearestFielder.getPos().sub(ball.getPos()).mag()) {
                nearestFielder = fielder;
            }
        }
        restFielders = (RobotList<Ally>) fielders.clone();
        if(nearestFielder != null) {
            restFielders.remove(nearestFielder);
        }

        if(nearestFielder == null || restFielders == null) {
            return false;
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
//                ArrayList<Vec2D> gapPos = gapFinder.getTopNMaxPosWithClearance(restFielders.size(), interAllyClearance);
//                if(gapPos != null) {
//                    new Swarm(restFielders).groupTo(gapPos);
//                }
//            }
//
//            default -> {
//                state = 0;
//            }
//        }