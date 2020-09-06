package Triton.Computation;

import Triton.Config.PathfinderConfig;
import Triton.Shape.*;
import java.util.*;

public class Pathing {

    private static double END_THRESHOLD = 100;
    private static double MOVE_DIST = 100;
    private static int MAX_POINTS = 1000;

    public static ArrayList<Vec2D> computePathAngle(Vec2D start, Vec2D dest, ArrayList<Circle2D> obstacles) {
        ArrayList<Vec2D> points = new ArrayList<Vec2D>();
        Vec2D pos = new Vec2D(start);
        points.add(pos);
        while (Vec2D.dist(pos, dest) > END_THRESHOLD && points.size() < MAX_POINTS) {
            Vec2D dir = dest.sub(pos).norm();
            dir = dir.rotate(Math.random() * 2 - 1);
            Vec2D vel = dir.mult(MOVE_DIST);
            pos = pos.add(vel);
            points.add(pos);
        }
        return points;
    }

    public static ArrayList<Vec2D> computePathDist(Vec2D start, Vec2D dest, ArrayList<Circle2D> obstacles) {
        ArrayList<Vec2D> points = new ArrayList<Vec2D>();
        ArrayList<Circle2D> inRect = new ArrayList<Circle2D>();
        for (Circle2D obstacle : obstacles) {
            Vec2D p = obstacle.center;
            // check if the obstacle is not in between start and dest on x-axis
            if (!(p.x <= start.x + obstacle.radius && p.x >= dest.x - obstacle.radius) && 
            !(p.x >= start.x - obstacle.radius && p.x <= dest.x + obstacle.radius)) {
                continue;
            }
            // check if the obstacle is not in between start and dest on y-axis
            if (!(p.y <= start.y + obstacle.radius && p.y >= dest.y - obstacle.radius) && 
            !(p.y >= start.y - obstacle.radius && p.y <= dest.y + obstacle.radius)) {
                continue;
            }
            inRect.add(obstacle);
        }

        ArrayList<Circle2D> inRange = new ArrayList<Circle2D>();
        Line2D line = new Line2D(start, dest);
        for (Circle2D obstacle : inRect) {
            if (line.perpDist(obstacle.center) <= obstacle.radius * 2)
                inRange.add(obstacle);
        }

        Circle2D closestObsacle;
        double min = Double.MAX_VALUE;
        for (Circle2D obstacle : inRange) {
            double dist2 = Vec2D.dist2(start, obstacle.center);
            if (dist2 < min) {
                closestObsacle = obstacle;
                min = dist2;
            }
        }
        // TODO: decide which side to go around the closestObsacle
        return points;
    }

    public static ArrayList<Vec2D> computePathVectorField(Vec2D start, Vec2D dest, ArrayList<Circle2D> obstacles) {
        ArrayList<Vec2D> points = new ArrayList<Vec2D>();
        Vec2D pos = new Vec2D(start);
        points.add(pos);
        while (Vec2D.dist(pos, dest) > END_THRESHOLD && points.size() < MAX_POINTS) {
            Vec2D force = new Vec2D(0, 0);
            for (Circle2D circle : obstacles) {
                force = force.add(getPushForce(pos, circle.center, PathfinderConfig.PUSH_STRENGTH));
            }
            force = force.add(getPullForce(pos, dest, PathfinderConfig.PULL_STRENGTH));
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