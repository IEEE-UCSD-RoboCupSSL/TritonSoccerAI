package Triton.PeriphModules.GameControl.GameStates;

import Triton.CoreModules.Robot.Team;

public class GameState {
    protected Team team;
    private GameStateName name;

    public GameState (GameStateName name) {
        this.name = name;
    }

    public GameStateName getName() {
        return name;
    }
    public Team getTeam() {
        return team;
    }
}
