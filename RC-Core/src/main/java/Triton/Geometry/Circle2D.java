package Triton.Geometry;

import java.lang.Math;

public class Circle2D extends Shape2D {
    
    public Point2D center;
    public double radius;

    public Circle2D(Point2D center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    public boolean isInside(Point2D point) {
        double dist = Math.sqrt(Math.pow(point.x - center.x, 2) + Math.pow(point.y - center.y, 2));
        return dist < this.radius; 
    }
}