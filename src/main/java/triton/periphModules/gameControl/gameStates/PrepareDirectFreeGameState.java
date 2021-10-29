package triton.periphModules.gameControl.gameStates;

import triton.coreModules.robot.Team;

public class PrepareDirectFreeGameState extends GameState {


    public PrepareDirectFreeGameState(Team team) {
        super(GameStateName.PREPARE_DIRECT_FREE);
        this.team = team;
    }

    public PrepareDirectFreeGameState() {
        this(Team.BLUE);
    }

}
