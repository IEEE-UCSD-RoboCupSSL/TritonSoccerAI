package Triton.PeriphModules.GameControl.GameStates;

import Triton.CoreModules.Robot.Team;

public class PenaltyGameState extends GameState {
    private Team team;

    public PenaltyGameState(Team team) {
        super(GameStateName.PENALTY);
        this.team = team;
    }

    public PenaltyGameState() {
        this(Team.BLUE);
    }

    public Team getTeam() {
        return team;
    }
}
