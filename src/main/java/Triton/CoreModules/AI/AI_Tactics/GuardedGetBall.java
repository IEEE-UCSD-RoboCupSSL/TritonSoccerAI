package Triton.CoreModules.AI.AI_Tactics;

import Triton.CoreModules.AI.Estimators.Estimator;
import Triton.CoreModules.AI.Estimators.PassEstimator;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;

public class GuardedGetBall extends Tactics{

    public GuardedGetBall(RobotList<Ally> allies, Ally keeper, RobotList<Foe> foes, Ball ball, Estimator estimator, PassEstimator passEstimator) {
        super(allies, keeper, foes, ball, estimator, passEstimator);
    }

    @Override
    public boolean exec() {
        return false;
    }
}
