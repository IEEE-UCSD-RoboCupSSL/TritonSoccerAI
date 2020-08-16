package Triton.Computation;

import Triton.Shape.*;

import java.util.*;

public class Pathing {
    private static double END_THRESHOLD = 1;
    private static double MOVE_DIST = 1;

    public static Vec2D[] computePath(Vec2D start, Vec2D des, Shape2D[] obstacles) {
        Vec2D pos = new Vec2D(start);

        ArrayList<Vec2D> path = new ArrayList<Vec2D>();
        path.add(pos);
        while (Vec2D.dist(pos, des) > END_THRESHOLD) {
            Vec2D dir = des.sub(pos).norm();
            Vec2D vel = dir.mult(MOVE_DIST);
            pos.add(vel);
            path.add(pos);
        }
        return (Vec2D[]) path.toArray();
    }
}