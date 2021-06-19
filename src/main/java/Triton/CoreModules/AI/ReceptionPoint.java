package Triton.CoreModules.AI;

import Triton.CoreModules.Robot.Ally.KickType;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import lombok.Getter;

@Getter
public class ReceptionPoint {
    private final Vec2D receptionPoint;
    private final double angle;
    private final Vec2D kickVec;
    private final boolean isStart;

    /**
     * If `isDummy` is true, then all other fields should be ignored as they contain dummy values
     *
     * @param receptionPoint
     * @param angle
     * @param kickVec
     * @param isStart
     */
    public ReceptionPoint(Vec2D receptionPoint, double angle, Vec2D kickVec, boolean isStart) {
        this.receptionPoint = receptionPoint;
        this.angle = angle;
        this.kickVec = kickVec;
        this.isStart = isStart;
    }
}
