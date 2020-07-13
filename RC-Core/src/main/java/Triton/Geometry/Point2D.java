package Triton.Geometry;

public class Point2D extends Vec2D {

    public Point2D(double x, double y) {
        super(x,y); 
    }

    public Point2D(Point2D p) {
       super(p.x, p.y);
    }

    public Point2D subtract(Point2D toSubtract) {
        return new Point2D(this.x - toSubtract.x, this.y - toSubtract.y);
    }

    public Point2D multiply(double z) {
        return new Point2D(this.x * z, this.y * z);
    }

    @Override
    public String toString() {
        String s = "";
        s += "(" + x + ", " + y + ")";
        return s;
    }


}