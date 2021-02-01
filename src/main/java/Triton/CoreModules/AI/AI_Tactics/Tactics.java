package Triton.CoreModules.AI.AI_Tactics;


import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.PassEstimator;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;

public abstract class Tactics {

    protected final RobotList<Ally> allies;
    protected final RobotList<Foe> foes;
    protected final Ally keeper;
    protected final Ball ball;
    protected final BasicEstimator basicEstimator;
    protected final PassEstimator passEstimator;

    public Tactics(RobotList<Ally> allies, Ally keeper, RobotList<Foe> foes,
                   Ball ball, BasicEstimator basicEstimator, PassEstimator passEstimator) {
        this.allies = allies;
        this.foes = foes;
        this.ball = ball;
        this.keeper = keeper;
        this.basicEstimator = basicEstimator;
        this.passEstimator = passEstimator;
    }

    abstract public boolean exec();
}
