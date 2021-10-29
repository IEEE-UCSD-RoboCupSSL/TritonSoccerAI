package triton.periphModules.gameControl.gameStates;

import triton.coreModules.robot.Team;

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
