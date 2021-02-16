package Triton.CoreModules.AI.AI_Strategies;

import Triton.CoreModules.AI.AI_Tactics.Tactics;

public abstract class Strategies {

    protected Tactics getBall;
    protected Tactics attack;
    protected Tactics defend;

    abstract public void play();

    public Tactics getGetBallTactics(){
        return getBall;
    }

    public Tactics getAttackTactics() {
        return attack;
    }

    public Tactics getDefendTactics() {
        return defend;
    }

}
