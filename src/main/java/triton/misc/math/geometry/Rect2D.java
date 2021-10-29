package triton.misc.math.geometry;

import triton.misc.math.coordinates.Gridify;
import triton.misc.math.linearAlgebra.Vec2D;

import java.awt.*;

/**
 * Represents a 2D rectangle
 */
public class Rect2D extends Geometry2D {

    public final Vec2D anchor;
    public final double width;
    public final double height;

    /**
     * Constructs a Rect2D with specified left edge and bottom edge
     *
     * @param left   left edge of the rectangle
     * @param bottom bottom edge of the rectangle
     */
    public Rect2D(Line2D left, Line2D bottom) {
        this.anchor = left.p1;
        this.width = bottom.length();
        this.height = left.length();
    }

    /**
     * Constructs a Rect2D with specified bottom left point, width, and height
     *
     * @param point  bottom left point
     * @param width  width of rectangle
     * @param height height of rectangle
     */
    public Rect2D(Vec2D point, double width, double height) {
        this.anchor = point;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns true if point is within the rectangle
     *
     * @param point the point to check
     * @return true if point is within the rectangle, false otherwise
     */
    @Override
    public boolean isInside(Vec2D point) {
        return (point.x >= anchor.x) && (point.x <= anchor.x + width) && (point.y >= anchor.y) && (point.y <= anchor.y + height);
    }

    /**
     * @return the rectangle as a string representation
     */
    @Override
    public String toString() {
        String s = "";
        s += "[" + anchor + ", " + width + ", " + height + "]";
        return s;
    }

    public double distTo(Vec2D pos) {
        double dx = Math.max(Math.max(anchor.x - pos.x, pos.x - (anchor.x + width)), 0.0);
        double dy = Math.max(Math.max(anchor.y - pos.y, pos.y - (anchor.y + height)), 0.0);
        return Math.sqrt(dx*dx + dy*dy);
    }

    @Override
    public void draw(Graphics2D g2d, Gridify convert) {
        int[] displayAnchor = convert.fromPos(anchor);
        int displayWidth = convert.numCols(width);
        int displayHeight = convert.numRows(height);
        g2d.fillRect(displayAnchor[0], displayAnchor[1], displayWidth, displayHeight);
    }
}
        