package Triton.CoreModules.AI.AI_Tactics;

import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.PassEstimator;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;

public class HugFoeGetBall extends Tactics {

    protected final BasicEstimator basicEstimator;
    protected final PassEstimator passEstimator;
    public HugFoeGetBall(RobotList<Ally> allies, Ally keeper, RobotList<Foe> foes, Ball ball) {
        super(allies, keeper, foes, ball);
        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        passEstimator = new PassEstimator(fielders, keeper, foes, ball);
    }

    @Override
    public boolean exec() {

        return false;
    }
}
