package triton.periphModules.gameControl.gameStates;

import triton.coreModules.robot.Team;

public class PreparePenaltyGameState extends GameState {

    public PreparePenaltyGameState(Team team) {
        super(GameStateName.PREPARE_PENALTY);
        this.team = team;
    }

    public PreparePenaltyGameState() {
        this(Team.BLUE);
    }

}
