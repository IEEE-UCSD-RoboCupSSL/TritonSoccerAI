package Triton.CoreModules.AI.Strategies.Attack;

import Triton.CoreModules.AI.Strategies.Strategies;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;


public class BasicAttack extends Strategies {

    private final RobotList<Ally> allies;
    private final RobotList<Foe> foes;
    private final Ball ball;

    public BasicAttack(RobotList<Ally> allies, RobotList<Foe> foes, Ball ball) {
        this.allies = allies;
        this.foes = foes;
        this.ball = ball;
    }

    @Override
    public void play() {

    }
}
