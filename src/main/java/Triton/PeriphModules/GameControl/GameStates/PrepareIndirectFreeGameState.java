package Triton.PeriphModules.GameControl.GameStates;

import Triton.CoreModules.Robot.Team;

public class PrepareIndirectFreeGameState extends GameState {

    public PrepareIndirectFreeGameState(Team team) {
        super(GameStateName.PREPARE_INDIRECT_FREE);
        this.team = team;
    }

    public PrepareIndirectFreeGameState() {
        this(Team.BLUE);
    }

}
