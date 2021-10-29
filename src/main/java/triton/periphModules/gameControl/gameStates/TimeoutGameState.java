package triton.periphModules.gameControl.gameStates;

import triton.coreModules.robot.Team;

public class TimeoutGameState extends GameState {

    public TimeoutGameState(Team team) {
        super(GameStateName.TIMEOUT);
        this.team = team;
    }

    public TimeoutGameState() {
        this(Team.BLUE);
    }

}
