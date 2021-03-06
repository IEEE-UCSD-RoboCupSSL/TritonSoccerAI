package Triton.PeriphModules.GameControl.GameStates;

import Triton.CoreModules.Robot.Team;

public class KickoffGameState extends GameState {
    private Team team;

    public KickoffGameState() {
        this(Team.BLUE);
    }

    public KickoffGameState(Team team) {
        super(GameStateName.KICKOFF);
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }
}
