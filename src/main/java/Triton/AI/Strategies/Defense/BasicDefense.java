package Triton.AI.Strategies.Defense;

import Triton.AI.Strategies.Strategies;
import Triton.MovingObjectModules.Ball.Ball;
import Triton.MovingObjectModules.Robot.Ally;
import Triton.MovingObjectModules.Robot.Foe;

import java.util.ArrayList;

public class BasicDefense extends Strategies {

    private final ArrayList<Ally> allies;
    private final ArrayList<Foe> foes;
    private final Ally keeper;
    private final Ball ball;

    public BasicDefense(ArrayList<Ally> allies, Ally keeper, ArrayList<Foe> foes, Ball ball) {
        this.allies = allies;
        this.foes = foes;
        this.ball = ball;
        this.keeper = keeper;
    }


    @Override
    public void play() {

    }
}
