package Triton.CoreModules.AI;

import Triton.Misc.Math.LinearAlgebra.Vec2D;
import lombok.Getter;

@Getter
public class ReceptionPoint {
    private final Vec2D point;
    private final double angle;
    private final Vec2D kickVec;
    private final boolean isStart;

    /**
     * If `isDummy` is true, then all other fields should be ignored as they contain dummy values
     *
     * @param point
     * @param angle
     * @param kickVec
     * @param isStart
     */
    public ReceptionPoint(Vec2D point, double angle, Vec2D kickVec, boolean isStart) {
        this.point = point;
        this.angle = angle;
        this.kickVec = kickVec;
        this.isStart = isStart;
    }
}
