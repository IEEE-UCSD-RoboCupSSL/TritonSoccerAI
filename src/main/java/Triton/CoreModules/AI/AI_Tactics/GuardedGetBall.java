package Triton.CoreModules.AI.AI_Tactics;

import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.PassInfo;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;

public class GuardedGetBall extends Tactics {

    protected final BasicEstimator basicEstimator;
    protected final PassInfo passInfo;

    public GuardedGetBall(RobotList<Ally> allies, Ally keeper, RobotList<Foe> foes, Ball ball) {
        super(allies, keeper, foes, ball);
        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        passInfo = new PassInfo(fielders, foes, ball);
    }

    @Override
    public boolean exec() {

        return false;
    }
}
