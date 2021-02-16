package Triton.CoreModules.AI.AI_Tactics;


import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;

public abstract class Tactics {

    protected final RobotList<Ally> fielders;
    protected final RobotList<Foe> foes;
    protected final Ally keeper;
    protected final Ball ball;

    public Tactics(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes,
                   Ball ball) {
        this.fielders = fielders;
        this.foes = foes;
        this.ball = ball;
        this.keeper = keeper;
    }

    abstract public boolean exec();
}
