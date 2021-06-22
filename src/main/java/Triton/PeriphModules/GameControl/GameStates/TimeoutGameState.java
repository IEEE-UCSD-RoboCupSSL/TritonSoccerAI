package Triton.PeriphModules.GameControl.GameStates;

import Triton.CoreModules.Robot.Team;

public class TimeoutGameState extends GameState {

    public TimeoutGameState(Team team) {
        super(GameStateName.TIMEOUT);
        this.team = team;
    }

    public TimeoutGameState() {
        this(Team.BLUE);
    }

}
