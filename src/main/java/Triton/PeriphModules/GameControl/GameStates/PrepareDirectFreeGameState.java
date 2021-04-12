package Triton.PeriphModules.GameControl.GameStates;

import Triton.CoreModules.Robot.Team;

public class PrepareDirectFreeGameState extends GameState {
    private Team team;

    public PrepareDirectFreeGameState(Team team) {
        super(GameStateName.PREPARE_DIRECT_FREE);
        this.team = team;
    }

    public PrepareDirectFreeGameState() {
        this(Team.BLUE);
    }

    public Team getTeam() {
        return team;
    }
}
