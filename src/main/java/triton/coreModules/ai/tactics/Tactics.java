package triton.coreModules.ai.tactics;


import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.RobotList;

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
