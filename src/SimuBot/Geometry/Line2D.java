package SimuBot.Geometry;

public class Line2D {
    
    public Point2D p1, p2; 
    private String name;
    private double thickness;

    public Line2D(Point2D p1, Point2D p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Line2D(double p1x, double p1y, double p2x, double p2y) {
        p1 = new Point2D(p1x, p1y);
        p2 = new Point2D(p2x, p2y);
    }

    public Line2D(Line2D line) {
        p1 = line.p1;
        p2 = line.p2;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setThickness(double thickness) {
        this.thickness = thickness;
    }
    public double getThickness() {
        return thickness;
    }

    @Override public String toString() {
        String s = "";
        s += "[" + p1 + ", " + p2 + "]";
        return s;
    }

}