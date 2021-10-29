package triton.periphModules.gameControl.gameStates;

import triton.coreModules.robot.Team;

public class PrepareIndirectFreeGameState extends GameState {

    public PrepareIndirectFreeGameState(Team team) {
        super(GameStateName.PREPARE_INDIRECT_FREE);
        this.team = team;
    }

    public PrepareIndirectFreeGameState() {
        this(Team.BLUE);
    }

}
