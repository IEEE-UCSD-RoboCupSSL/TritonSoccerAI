package triton.misc.math.geometry;

import triton.misc.math.coordinates.Gridify;
import triton.misc.math.linearAlgebra.Vec2D;

import java.awt.*;

/**
 * Represents a 2D line
 */
public class Line2D implements Drawable2D {

    public final Vec2D p1;
    public final Vec2D p2;
    private String name;
    private double thickness;

    /**
     * Constructs a Line2D with specified endpoints
     *
     * @param p1 point 1
     * @param p2 point 2
     */
    public Line2D(Vec2D p1, Vec2D p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    /**
     * Constructs a Line2D with specified endpoints as x and y coordinates
     *
     * @param p1x x coordinate of point 1
     * @param p1y y coordinate of point 1
     * @param p2x x coordinate of point 2
     * @param p2y y coordinate of point 2
     */
    public Line2D(double p1x, double p1y, double p2x, double p2y) {
        p1 = new Vec2D(p1x, p1y);
        p2 = new Vec2D(p2x, p2y);
    }

    /**
     * Constructs a deep copy of a given line
     *
     * @param line line to copy from
     */
    public Line2D(Line2D line) {
        p1 = line.p1;
        p2 = line.p2;
        name = line.name;
        thickness = line.thickness;
    }

    /**
     * Returns the direction the line points toward, starting at point 1 to point 2
     *
     * @return the direction from point 1 to point 2
     */
    public Vec2D getDir() {
        return p2.sub(p1).normalized();
    }

    /**
     * Returns the shortest distance from a specified point to any point on the line
     *
     * @param point the point to check
     * @return the shortest distance from the point to the line
     */
    public double perpDist(Vec2D point) {
        double[] eqn = toEqn();
        return Math.abs((eqn[0] * point.x + eqn[1] * point.y + eqn[2]) / Math.sqrt(eqn[0] * eqn[0] + eqn[1] * eqn[1]));
    }

    /**
     * Return the line as an equation with format Ax + By = C
     *
     * @return values A, B, and C as an array in that order
     */
    public double[] toEqn() {
        double A = p1.y - p2.y;
        double B = p2.x - p1.x;
        double C = p1.x * p2.y - p2.x * p1.y;
        return new double[]{A, B, C};
    }

    /**
     * Returns the line split into two at its midpoint
     *
     * @return an array corresponding to the first and second line
     */
    public Line2D[] split() {
        Line2D[] results;
        if (this.p1.x == this.p2.x) { // Vertical line
            Line2D topLine = new Line2D(p1.x, p1.y, p2.x, (p1.y + p2.y) / 2);
            topLine.name = "Top" + this.name;
            Line2D bottomLine = new Line2D(p1.x, (p1.y + p2.y) / 2, p2.x, p2.y);
            bottomLine.name = "Bottom" + this.name;
            results = new Line2D[]{topLine, bottomLine};
        } else { // Horizontal line
            Line2D leftLine = new Line2D(p1.x, p1.y, (p1.x + p2.x) / 2, p2.y);
            leftLine.name = "Left" + this.name;
            Line2D rightLine = new Line2D((p1.x + p2.x) / 2, p1.y, p2.x, p2.y);
            rightLine.name = "Right" + this.name;
            results = new Line2D[]{leftLine, rightLine};
        }
        return results;
    }

    /**
     * Returns the midpoint of the line
     *
     * @return the midpoint of the line
     */
    public Vec2D midpoint() {
        return new Vec2D((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
    }

    /**
     * Returns the length of the line
     *
     * @return the length of the line
     */
    public double length() {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

    /**
     * @return the name of the line
     */
    public String getName() {
        return name;
    }

    /**
     * @param name name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the thickness of the line
     */
    public double getThickness() {
        return thickness;
    }

    /**
     * @param thickness thickness to set
     */
    public void setThickness(double thickness) {
        this.thickness = thickness;
    }

    /**
     * @return the line as a string
     */
    @Override
    public String toString() {
        String s = "";
        s += "[" + p1 + ", " + p2 + "]";
        return s;
    }

    @Override
    public void draw(Graphics2D g2d, Gridify convert) {
        int[] displayPoint1 = convert.fromPos(p1);
        int[] displayPoint2 = convert.fromPos(p2);
        g2d.drawLine(displayPoint1[0], displayPoint1[1], displayPoint2[0], displayPoint2[1]);
    }
}