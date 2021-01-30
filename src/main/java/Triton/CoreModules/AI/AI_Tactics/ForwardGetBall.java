package Triton.CoreModules.AI.AI_Tactics;

import Triton.CoreModules.AI.Estimators.Estimator;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;

public class ForwardGetBall extends Tactics{

    @Override
    public boolean exec(RobotList<Ally> allies, RobotList<Foe> foes, Ball ball, Estimator estimator) {
        return false;
    }
}
