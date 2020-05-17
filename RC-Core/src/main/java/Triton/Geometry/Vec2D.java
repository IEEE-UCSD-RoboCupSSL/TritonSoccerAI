package Triton.Geometry;

public class Vec2D {
    public double x,y; 
    private String name;
    public Vec2D(double x, double y) {
        this.x = x;
        this.y = y;   
    }

    public Vec2D(Point2D p) {
        this.x = p.x;
        this.y = p.y;
    }

    void setName(String name) {
        this.name = name;
    }
    String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        String s = "";
        s += "<" + x + ", " + y + ">";
        return s;
    }
}