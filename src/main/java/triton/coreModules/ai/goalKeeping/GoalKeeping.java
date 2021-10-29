package triton.coreModules.ai.goalKeeping;

import triton.coreModules.ai.estimators.BasicEstimator;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.misc.math.linearAlgebra.Vec2D;

import static triton.config.oldConfigs.ObjectConfig.MAX_KICK_VEL;

public class GoalKeeping {

    private final Ally keeper;
    private final Ball ball;
    private final BasicEstimator basicEstimator;

    public GoalKeeping(Ally keeper, Ball ball, BasicEstimator basicEstimator) {
        this.keeper = keeper;
        this.ball = ball;
        this.basicEstimator = basicEstimator;
    }

    public void moveToStart() {
        keeper.curveTo(new Vec2D(0, -4200), 0);
    }

    public void passiveGuarding() {
        if(keeper.isHoldingBall()) {
//            Ally nearestBot = basicEstimator.getNearestFielderToBall();
//            if(nearestBot == null) return;

            if(keeper.isDirAimed(0)) {
                keeper.kick(new Vec2D(MAX_KICK_VEL, MAX_KICK_VEL));
            } else {
                keeper.rotateTo(0);
            }
        } else {
            keeper.keep(ball, basicEstimator.getAimTrajectory());
        }
    }

    public void activeGuarding() {
        if (basicEstimator.getBallHolder() == null || basicEstimator.getBallHolder() instanceof Ally) {
            // safer situation
        } else {
            // more dangerous situation
        }
    }


}
