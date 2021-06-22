package Triton.ErForceInterfaces;

import Triton.Misc.Math.LinearAlgebra.Vec2D;

public interface AvoidLine {
    boolean isOutside(Vec2D loc, double minDistToLine);
}
