package Triton.PeriphModules.GameControl.GameStates;

import Triton.CoreModules.Robot.Team;

public class PreparePenaltyGameState extends GameState {
    private Team team;

    public PreparePenaltyGameState(Team team) {
        super(GameStateName.PREPARE_PENALTY);
        this.team = team;
    }

    public PreparePenaltyGameState() {
        this(Team.BLUE);
    }

    public Team getTeam() {
        return team;
    }
}
