package triton.coreModules.ai.strategies;

import triton.coreModules.ai.tactics.Tactics;

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
