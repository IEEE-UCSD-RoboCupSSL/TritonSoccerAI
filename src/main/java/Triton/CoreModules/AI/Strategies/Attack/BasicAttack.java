package Triton.CoreModules.AI.Strategies.Attack;

import Triton.CoreModules.AI.Strategies.Strategies;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;

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
