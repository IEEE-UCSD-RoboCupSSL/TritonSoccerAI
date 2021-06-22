package Triton.PeriphModules.GameControl.GameStates;

import Triton.CoreModules.Robot.Team;

public class PrepareKickoffGameState extends GameState {

    public PrepareKickoffGameState(Team team) {
        super(GameStateName.PREPARE_KICKOFF);
        this.team = team;
    }

    public PrepareKickoffGameState() {
        this(Team.BLUE);
    }
}
