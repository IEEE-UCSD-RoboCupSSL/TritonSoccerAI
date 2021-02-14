package Triton.CoreModules.AI.AI_Tactics;

import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.PassEstimator;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;

public class FillGapGetBall extends Tactics {

    public FillGapGetBall(RobotList<Ally> allies, Ally keeper, RobotList<Foe> foes, Ball ball, BasicEstimator basicEstimator, PassEstimator passEstimator) {
        super(allies, keeper, foes, ball, basicEstimator, passEstimator);
    }

    @Override
    public boolean exec() {
        return false;
    }
}