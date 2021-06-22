package Triton.PeriphModules.GameControl.GameStates;

import Triton.CoreModules.Robot.Team;

public class PreparePenaltyGameState extends GameState {

    public PreparePenaltyGameState(Team team) {
        super(GameStateName.PREPARE_PENALTY);
        this.team = team;
    }

    public PreparePenaltyGameState() {
        this(Team.BLUE);
    }

}
