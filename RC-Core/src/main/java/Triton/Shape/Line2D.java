package Triton.Shape;

import java.lang.Math;
import Proto.RemoteCommands;

public class Line2D {
    
    public Vec2D p1, p2; 
    private String name;
    private double thickness;

    public Line2D(Vec2D p1, Vec2D p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Line2D(double p1x, double p1y, double p2x, double p2y) {
        p1 = new Vec2D(p1x, p1y); 
        p2 = new Vec2D(p2x, p2y);
    }

    public double[] toEqn() {
        double A = p1.y - p2.y;
        double B = p2.x - p1.x;
        double C = p1.x * p2.y - p2.x * p1.y;
        return new double[] {A, B, C};
    }

    public Vec2D getDir() {
        return p2.sub(p1).norm();
    }

    public double perpDist(Vec2D point) {
        double[] eqn = toEqn();
        return Math.abs((eqn[0] * point.x + eqn[1] * point.y + eqn[2]) / Math.sqrt(eqn[0] * eqn[0] + eqn[1] * eqn[1]));
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

    public Line2D[] split() {
        if(this.p1.x == this.p2.x) { // Vertical line
            Line2D topLine = new Line2D(p1.x, p1.y, p2.x, (p1.y+p2.y)/2);
            topLine.name = "Top" + this.name;
            Line2D bottomLine = new Line2D(p1.x, (p1.y+p2.y)/2, p2.x, p2.y);
            bottomLine.name = "Bottom" + this.name;
            Line2D[] results = {topLine, bottomLine};
            return results;
        } else { // Horizontal line
            Line2D leftLine = new Line2D(p1.x, p1.y, (p1.x+p2.x)/2, p2.y);
            leftLine.name = "Left" + this.name;
            Line2D rightLine = new Line2D((p1.x+p2.x)/2, p1.y, p2.x, p2.y);
            rightLine.name = "Right" + this.name;
            Line2D[] results = {leftLine, rightLine};
            return results;
        }
    } 

    public Vec2D midpoint() {
        return new Vec2D((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
    }
    
    public double length() {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

    public RemoteCommands.Line2D toProto() {
        RemoteCommands.Line2D.Builder toSend = RemoteCommands.Line2D.newBuilder();
        toSend.setP1(p1.toProto());
        toSend.setP2(p2.toProto());
        return toSend.build();
    }

}