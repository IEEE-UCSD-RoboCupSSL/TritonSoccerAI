package Triton.CoreModules.AI;

import Triton.CoreModules.Robot.Ally.KickType;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import lombok.Getter;

@Getter
public class ReceptionPoint {
    private final Vec2D receptionPoint;
    private final double angle;
    private final KickType kickType;
    private final boolean isDummy;

    /**
     * If `isDummy` is true, then all other fields should be ignored as they contain dummy values
     *
     * @param receptionPoint
     * @param angle
     * @param kickType
     * @param isDummy
     */
    public ReceptionPoint(Vec2D receptionPoint, double angle, KickType kickType, boolean isDummy) {
        this.receptionPoint = receptionPoint;
        this.angle = angle;
        this.kickType = kickType;
        this.isDummy = isDummy;
    }
}
