package Triton.ErForceInterfaces;

import Triton.Misc.Math.Geometry.Line2D;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

public class AvoidLineImpl implements AvoidLine{

    private final Line2D lineToAvoid;

    public AvoidLineImpl(Line2D lineToAvoid) {
        this.lineToAvoid = lineToAvoid;
    }

    @Override
    public boolean isOutside(Vec2D loc, double minDistToLine) {
        double v = lineToAvoid.perpDist(loc);

        return v > minDistToLine;
    }
}
