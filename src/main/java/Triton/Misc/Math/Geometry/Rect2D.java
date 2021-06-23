package Triton.Misc.Math.Geometry;

import Triton.Misc.Math.Coordinates.Gridify;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * Represents a 2D rectangle
 */
public class Rect2D extends Geometry2D {

    public final Vec2D anchor;
    public final double width;
    public final double height;

    /**
     * Constructs a Rect2D with two parallel edges
     */
    public Rect2D(Line2D edge1, Line2D edge2) {
        if (edge1.p1.x == edge2.p1.x && edge1.p2.x == edge2.p2.x &&
            edge1.p1.y == edge1.p2.y && edge2.p1.y == edge2.p2.y) {
            // two horizontal lines
            if (edge2.p1.y > edge1.p1.y) {
                // swap lines to keep edge2 lower than edge1
                Line2D temp = edge2;
                edge2 = edge1;
                edge1 = temp;
            }
            boolean flag = edge2.p2.x - edge2.p1.x < 0;
            this.anchor = flag ? edge2.p2 : edge2.p1;
            this.width = flag ? edge2.p1.x - edge2.p2.x : edge2.p2.x - edge2.p1.x;
            this.height = Math.abs(edge1.p1.y - edge2.p1.y);
        } else if (edge1.p1.y == edge2.p1.y && edge1.p2.y == edge2.p2.y &&
                   edge1.p1.x == edge1.p2.x && edge2.p1.x == edge2.p2.x) {
            // two vertical lines
            if (edge2.p1.x > edge1.p1.x) {
                // swap lines to keep edge2 lefter than edge1
                Line2D temp = edge2;
                edge2 = edge1;
                edge1 = temp;
            }
            boolean flag = edge2.p2.y - edge2.p1.y < 0;
            this.anchor = flag ? edge2.p2 : edge2.p1;
            this.height = flag ? edge2.p1.y - edge2.p2.y : edge2.p2.y - edge2.p1.y;
            this.width = Math.abs(edge1.p1.x - edge2.p1.x);
        } else {
            System.err.println("unqualified edges for Rect2D");
            this.anchor = null;
            this.height = 0;
            this.width = 0;
        }
    }

    public Rect2D extend(double xExtend, double yExtend) {
        return new Rect2D(
            this.anchor.sub(new Vec2D(xExtend, yExtend)),
            this.width  + 2.0 * xExtend,
            this.height + 2.0 * yExtend
        );
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
        