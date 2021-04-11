package Triton.PeriphModules.GameControl.GameStates;

import Triton.CoreModules.Robot.Team;

public class FreeKickGameState extends GameState {
    private Team team;

    public FreeKickGameState(Team team) {
        super(GameStateName.FREE_KICK);
        this.team = team;
    }

    public FreeKickGameState() {
        this(Team.BLUE);
    }

    public Team getTeam() {
        return team;
    }
}
