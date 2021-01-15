package Triton.AI.GoalKeeping;

import Triton.AI.Estimators.Estimator;
import Triton.Objects.Ally;
import Triton.Objects.Ball;
import Triton.Objects.Foe;

public class GoalKeeping {

    private final Ally goalKeeper;
    private final Ball ball;
    private final Estimator estimator;

    public GoalKeeping(Ally goalKeeper, Ball ball, Estimator estimator) {
        this.goalKeeper = goalKeeper;
        this.ball = ball;
        this.estimator = estimator;
    }


    public void passiveGuarding() {
        if(estimator.getBallHolder() == null || estimator.getBallHolder() instanceof Ally) {
            // safer situation
        }
        else {
            // more dangerous situation
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
