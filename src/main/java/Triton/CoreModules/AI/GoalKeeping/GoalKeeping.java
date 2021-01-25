package Triton.CoreModules.AI.GoalKeeping;

import Triton.CoreModules.AI.Estimators.Estimator;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.Misc.Coordinates.Vec2D;

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
//        }
//        else {
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

        double targetAngle = ballTraj.mult(-1).toPlayerAngle();
        keeper.pathTo(targetPos, targetAngle);
//        }
    }

    public void activeGuarding() {
        if (estimator.getBallHolder() == null || estimator.getBallHolder() instanceof Ally) {
            // safer situation
        } else {
            // more dangerous situation
        }
    }


}
