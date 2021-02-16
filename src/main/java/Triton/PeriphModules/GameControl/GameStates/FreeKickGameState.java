package Triton.PeriphModules.GameControl.GameStates;

import Triton.CoreModules.Robot.Team;

public class FreeKickGameState extends GameState {
    private Team team;

    public FreeKickGameState() {
        this(Team.BLUE);
    }

    public FreeKickGameState(Team team) {
        super(GameStateName.FREE_KICK);
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }
}
