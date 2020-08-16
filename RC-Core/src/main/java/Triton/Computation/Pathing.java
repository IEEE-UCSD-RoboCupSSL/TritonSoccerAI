package Triton.Computation;

import Triton.Shape.*;

import java.util.*;

public class Pathing {
    private static double END_THRESHOLD = 50;
    private static double MOVE_DIST = 100;

    public static ArrayList<Vec2D> computePath(Vec2D start, Vec2D des, ArrayList<Shape2D> obstacles) {
        Vec2D pos = new Vec2D(start);

        ArrayList<Vec2D> points = new ArrayList<Vec2D>();
        points.add(pos);
        while (Vec2D.dist(pos, des) > END_THRESHOLD) {
            Vec2D dir = des.sub(pos).norm();
            Vec2D vel = dir.mult(MOVE_DIST);
            vel = vel.add(new Vec2D(Math.random() * 100 - 50, Math.random() * 100 - 50));
            pos = pos.add(vel);
            points.add(pos);
        }
        return points;
    }
}