package Triton.Misc.Math.Geometry;

import Triton.Misc.Math.LinearAlgebra.Vec2D;

/**
 * Abstract point to represent various 2D shapes
 */
public abstract class Geometry2D {
    /**
     * Returns true if point is within shape
     *
     * @param point point to check
     * @return true if point is within shape
     */
    public abstract boolean isInside(Vec2D point);
}