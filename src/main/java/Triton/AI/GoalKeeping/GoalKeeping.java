package Triton.AI.GoalKeeping;

import Triton.AI.Estimators.Estimator;
import Triton.Dependencies.Shape.Vec2D;
import Triton.Objects.Ally;
import Triton.Objects.Ball;

public class GoalKeeping {

    private final Ally keeper;
    private final Ball ball;
    private final Estimator estimator;

    public GoalKeeping(Ally keeper, Ball ball, Estimator estimator) {
        this.keeper = keeper;
        this.ball = ball;
        this.estimator = estimator;
    }


    public void passiveGuarding() {
        if(estimator.getBallHolder() == null || estimator.getBallHolder() instanceof Ally) {
            // safer situation
        }
        else {
            // more dangerous situation
            Vec2D ballTraj = estimator.getAimTrajectory();

        }
    }

    public void activeGuarding() {
        if(estimator.getBallHolder() == null || estimator.getBallHolder() instanceof Ally) {
            // safer situation
        }
        else {
            // more dangerous situation
        }
    }



}
