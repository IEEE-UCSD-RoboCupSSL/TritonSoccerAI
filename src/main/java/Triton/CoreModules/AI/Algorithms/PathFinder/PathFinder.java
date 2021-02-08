package Triton.CoreModules.AI.Algorithms.PathFinder;

import Triton.Misc.Math.Geometry.Circle2D;
import Triton.Misc.Math.Matrix.Vec2D;

import java.util.ArrayList;

public abstract class PathFinder {

    private final String name; // Algorithm name

    public PathFinder(String name) {
        this.name = name;
    }

    public abstract void setObstacles(ArrayList<Circle2D> obstacles);

    public abstract ArrayList<Vec2D> findPath(Vec2D startPos, Vec2D targetPos);

    public String getName() {
        return name;
    }
}
