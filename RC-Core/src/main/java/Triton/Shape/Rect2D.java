package Triton.Shape;

public class Rect2D extends Shape2D {
   
    public Vec2D anchor;
    public double width;
    public double height;

    public Rect2D(Line2D left, Line2D bottom) {
        this.anchor = left.p1;
        this.width = bottom.length();
        this.height = left.length();
    }

    public Rect2D(Vec2D point, double width, double height) {
        this.anchor = point;
        this.width = width;
        this.height = height;
    }

    @Override public String toString() {
        String s = "";
        s += "[" + anchor + ", " + width + ", " + height + "]";
        return s;
    }

    public boolean isInside(Vec2D point) {
        if (point.x < anchor.x || point.x > anchor.x + width || point.y < anchor.y || point.y > anchor.y + height)
            return false;
        return true;
    }
}
        