package Triton.Shape;

import java.lang.Math;

public class Circle2D extends Shape2D {
    
    public Vec2D center;
    public double radius;

    public Circle2D(Vec2D center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    public boolean isInside(Vec2D point) {
        double dist = Math.sqrt(Math.pow(point.x - center.x, 2) + Math.pow(point.y - center.y, 2));
        return dist < this.radius; 
    }
}