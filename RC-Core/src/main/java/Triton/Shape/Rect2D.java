package Triton.Shape;

/**
 * Represents a 2D rectangle
 */
public class Rect2D extends Shape2D {

    public Vec2D anchor;
    public double width;
    public double height;

    /**
     * Constructs a Rect2D with specified left edge and bottom edge
     * @param left left edge of the rectangle
     * @param bottom bottom edge of the rectangle
     */
    public Rect2D(Line2D left, Line2D bottom) {
        this.anchor = left.p1;
        this.width = bottom.length();
        this.height = left.length();
    }

    /**
     * Constructs a Rect2D with specified bottom left point, width, and height
     * @param point bottom left point
     * @param width width of rectangle
     * @param height height of rectangle
     */
    public Rect2D(Vec2D point, double width, double height) {
        this.anchor = point;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns true if point is within the rectangle
     * @param point the point to check
     * @return true if point is within the rectangle, false otherwise
     */
    @Override
    public boolean isInside(Vec2D point) {
        return !(point.x < anchor.x) && !(point.x > anchor.x + width) && !(point.y < anchor.y) && !(point.y > anchor.y + height);
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
}
        