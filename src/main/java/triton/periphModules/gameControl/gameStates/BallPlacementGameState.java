package triton.periphModules.gameControl.gameStates;

import triton.coreModules.robot.Team;
import triton.misc.math.linearAlgebra.Vec2D;

public class BallPlacementGameState extends GameState {

    private Vec2D targetPos;

    public BallPlacementGameState(Team team, Vec2D targetPos) {
        super(GameStateName.BALL_PLACEMENT);
        this.team = team;
        this.targetPos = targetPos;
    }

    public BallPlacementGameState() {
        this(Team.BLUE, new Vec2D(0, 0));
    }

    public BallPlacementGameState(Team team) {
        this(team, new Vec2D(0, 0));
    }


    public Vec2D getTargetPos() {
        return targetPos;
    }
}
