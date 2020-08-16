package Triton.Computation;

import Triton.Shape.*;

import java.util.*;

public class Pathing {
    
    private static double END_THRESHOLD = 100;
    private static double MOVE_DIST = 100;
    private static int MAX_POINTS = 1000;

    public static ArrayList<Vec2D> computePath(Vec2D start, Vec2D des, ArrayList<Shape2D> obstacles) {
        Vec2D pos = new Vec2D(start);

        ArrayList<Vec2D> points = new ArrayList<Vec2D>();
        points.add(pos);
        while (Vec2D.dist(pos, des) > END_THRESHOLD && points.size() < MAX_POINTS) {
            Vec2D dir = des.sub(pos).norm();
            dir = dir.rotate(Math.random() * 2 - 1);
            Vec2D vel = dir.mult(MOVE_DIST);
            pos = pos.add(vel);
            points.add(pos);
        }
        return points;
    }

    public static ArrayList<Vec2D> computePathVectorField(Vec2D start, Vec2D des, ArrayList<Shape2D> obstacles) {
        Vec2D pos = new Vec2D(start);

        ArrayList<Vec2D> points = new ArrayList<Vec2D>();
        points.add(pos);
        while (Vec2D.dist(pos, des) > END_THRESHOLD && points.size() < MAX_POINTS) {
            Vec2D force = new Vec2D(0, 0);
            for (Shape2D shape : obstacles) {
                Circle2D circle = (Circle2D) shape;
                force = force.add(getPushForce(pos, circle.center, PathingConfig.PUSH_STRENGTH));
            }
            force = force.add(getPullForce(pos, des, PathingConfig.PULL_STRENGTH));
            pos = pos.add(force);
            points.add(pos);
        }
        return points;
    }

    private static Vec2D getPushForce(Vec2D pos, Vec2D object, double strength) {
        double mag = -strength / Vec2D.dist2(pos, object);
        Vec2D dir = object.sub(pos);
        dir = dir.norm();
        return dir.mult(mag);
    }

    private static Vec2D getPullForce(Vec2D pos, Vec2D object, double strength) {
        double mag = strength;
        Vec2D dir = object.sub(pos);
        dir = dir.norm();
        return dir.mult(mag);
    }
}