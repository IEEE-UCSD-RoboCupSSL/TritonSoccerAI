package Triton.AI.Strategies.Attack;

import Triton.AI.Strategies.Strategies;
import Triton.MovingObjectModules.Ball.Ball;
import Triton.MovingObjectModules.Robot.Ally;
import Triton.MovingObjectModules.Robot.Foe;

import java.util.ArrayList;

public class BasicAttack extends Strategies {

    private final ArrayList<Ally> allies;
    private final ArrayList<Foe> foes;
    private final Ball ball;

    public BasicAttack(ArrayList<Ally> allies, ArrayList<Foe> foes, Ball ball) {
        this.allies = allies;
        this.foes = foes;
        this.ball = ball;
    }

    @Override
    public void play() {

    }
}
