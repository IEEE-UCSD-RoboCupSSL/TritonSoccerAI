package Triton.CoreModules.AI.AI_Tactics;


import Triton.CoreModules.AI.Estimators.Estimator;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;

public abstract class Tactics {
    abstract public boolean exec(RobotList<Ally> allies, RobotList<Foe> foes,
                                 Ball ball, Estimator estimator);
}