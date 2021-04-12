package Triton.PeriphModules.GameControl.GameStates;

import Triton.CoreModules.Robot.Team;

public class TimeoutGameState extends GameState {
    private Team team;

    public TimeoutGameState(Team team) {
        super(GameStateName.TIMEOUT);
        this.team = team;
    }

    public TimeoutGameState() {
        this(Team.BLUE);
    }

    public Team getTeam() {
        return team;
    }
}
