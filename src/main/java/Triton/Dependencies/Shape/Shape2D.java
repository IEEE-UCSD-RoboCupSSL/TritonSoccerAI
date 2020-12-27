package Triton.Dependencies.Shape;

/**
 * Abstract point to represent various 2D shapes
 */
public abstract class Shape2D {
    /**
     * Returns true if point is within shape
     * @param point point to check
     * @return true if point is within shape
     */
    public abstract boolean isInside(Vec2D point);
}