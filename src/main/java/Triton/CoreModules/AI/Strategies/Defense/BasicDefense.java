package Triton.CoreModules.AI.Strategies.Defense;

import Triton.CoreModules.AI.Strategies.Strategies;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;


public class BasicDefense extends Strategies {

    private final RobotList<Ally> allies;
    private final RobotList<Foe> foes;
    private final Ally keeper;
    private final Ball ball;

    public BasicDefense(RobotList<Ally> allies, Ally keeper, RobotList<Foe> foes, Ball ball) {
        this.allies = allies;
        this.foes = foes;
        this.ball = ball;
        this.keeper = keeper;
    }


    @Override
    public void play() {

    }
}
