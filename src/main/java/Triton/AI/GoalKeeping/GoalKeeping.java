package Triton.AI.GoalKeeping;

import Triton.AI.Estimators.Estimator;
import Triton.Dependencies.Shape.Vec2D;
import Triton.MovingObjectModules.Ball.Ball;
import Triton.MovingObjectModules.Robot.Ally;

import static java.lang.Math.abs;

public class GoalKeeping {

    private final Ally keeper;
    private final Ball ball;
    private final Estimator estimator;

    public GoalKeeping(Ally keeper, Ball ball, Estimator estimator) {
        this.keeper = keeper;
        this.ball = ball;
        this.estimator = estimator;
    }

    public void moveToStart() {
        keeper.sprintToAngle(new Vec2D(0, -4400), 0);
    }

    public void passiveGuarding() {
//        if (estimator.getBallHolder() == null || estimator.getBallHolder() instanceof Ally) {
//            // safer situation
//        }
//        else {
            // more dangerous situation
            Vec2D ballPos = ball.getData().getPos();
            Vec2D ballTraj = estimator.getAimTrajectory();
            Vec2D keeperPos = keeper.getData().getPos();
            Vec2D keeperLine = new Vec2D(1, 0);

            if (Math.abs(ballTraj.y) <= 0.0001) {
                return;
            } else if (Math.abs(ballTraj.x) <= 0.0001) {
                keeper.pathTo(new Vec2D(ballPos.x, keeperPos.y), 0);
                return;
            }

            double m1 = ballTraj.y / ballTraj.x;
            double b1 = ballPos.y - (ballPos.x / m1);

            double m2 = 0;
            double b2 = keeperPos.y;

            Vec2D targetPos = new Vec2D((b2 - b1) / m1, b2);

            Vec2D targetToBall = ballPos.sub(targetPos);
            keeper.pathTo(targetPos, targetToBall.toPlayerAngle());
//        }
    }

    public void activeGuarding() {
        if (estimator.getBallHolder() == null || estimator.getBallHolder() instanceof Ally) {
            // safer situation
        }
        else {
            // more dangerous situation
        }
    }



}
