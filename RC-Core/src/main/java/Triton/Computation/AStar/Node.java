package Triton.Computation.AStar;

import Triton.Shape.Vec2D;

public class Node {
    boolean walkable = true;
    Vec2D worldPos;

    public Node(Vec2D worldPos) {
        this.worldPos = worldPos;
    }

    public void setWalkable(boolean walkable) {
        this.walkable = walkable;
    }

    public boolean getWalkable() {
        return walkable;
    }

    public Vec2D getWorldPos() {
        return worldPos;
    }
}