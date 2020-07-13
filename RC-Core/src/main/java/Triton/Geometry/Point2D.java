package Triton.Geometry;

public class Point2D extends Vec2D {

    public Point2D(double x, double y) {
        super(x,y); 
    }

    public Point2D(Point2D p) {
       super(p.x, p.y);
    }

    @Override
    public String toString() {
        String s = "";
        s += "(" + x + ", " + y + ")";
        return s;
    }


}