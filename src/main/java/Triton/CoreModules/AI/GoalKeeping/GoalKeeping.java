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
//        if (estimator.getBallHolder() == null || estimator.getBallHolder() instanceof Ally) {
//        }
//        else {
        Vec2D ballPos = ball.getPos();
        Vec2D ballTraj = basicEstimator.getAimTrajectory();
        Vec2D keeperPos = keeper.getPos();
        double keeperY = -4200;

        if (Math.abs(ballTraj.y) <= 0.0001) {
            return;
        } else if (Math.abs(ballTraj.x) <= 0.0001) {
            keeper.strafeTo(new Vec2D(ballPos.x, keeperPos.y), 0);
            return;
        }

        double m1 = ballTraj.y / ballTraj.x;
        double b1 = ballPos.y - (ballPos.x * m1);

        double m2 = 0;
        double b2 = keeperY;

        double targetX = (b2 - b1) / m1;

        Vec2D targetPos = new Vec2D(targetX, b2);

        double targetAngle = ballTraj.scale(-1).toPlayerAngle();
        keeper.strafeTo(targetPos, targetAngle);
//        }
    }

    public void activeGuarding() {
        if (basicEstimator.getBallHolder() == null || basicEstimator.getBallHolder() instanceof Ally) {
            // safer situation
        } else {
            // more dangerous situation
        }
    }


}
