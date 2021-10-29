package triton.misc.math.geometry;

import triton.misc.math.coordinates.Gridify;
import triton.misc.math.linearAlgebra.Vec2D;

import java.awt.*;

/**
 * Represents a 2D circle
 */
public class Circle2D extends Geometry2D implements Drawable2D {

    public final Vec2D center;
    public final double radius;

    /**
     * Constructs a Circle2D at specified point with specified radius
     *
     * @param center point at the center of the circle
     * @param radius radius of the circle
     */
    public Circle2D(Vec2D center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    /**
     * Returns true if a specified point is inside the circle
     *
     * @param point point to check
     * @return true if point is inside the circle
     */
    @Override
    public boolean isInside(Vec2D point) {
        double dist = Math.sqrt(Math.pow(point.x - center.x, 2) + Math.pow(point.y - center.y, 2));
        return dist < this.radius;
    }

    @Override
    public void draw(Graphics2D g2d, Gridify convert) {
        int[] displayPos = convert.fromPos(center);
        int width = convert.numCols(radius) * 2;
        int height = convert.numRows(radius) * 2;
        g2d.fillArc(displayPos[0] - width / 2, displayPos[1] - height / 2, width, height, 0, 360);
    }
}