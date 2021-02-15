package Triton.CoreModules.AI.GoalKeeping;

import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.Misc.Math.Matrix.Vec2D;

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
        keeper.sprintTo(new Vec2D(0, -4200), 0);
    }

    public void passiveGuarding() {
        keeper.keep(ball, basicEstimator.getAimTrajectory());
    }

    public void activeGuarding() {
        if (basicEstimator.getBallHolder() == null || basicEstimator.getBallHolder() instanceof Ally) {
            // safer situation
        } else {
            // more dangerous situation
        }
    }


}
