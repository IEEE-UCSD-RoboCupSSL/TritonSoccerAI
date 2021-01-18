package Triton.AI.Algorithms.PathFinder.ThetaStar;

import Triton.Dependencies.Shape.Vec2D;

public class Node {
    private final Vec2D worldPos;
    private final int row;
    private final int col;
    private final boolean walkable = true;
    private double gCost, hCost, fCost;
    private Node parent;

    public Node(Vec2D worldPos, int row, int col) {
        this.row = row;
        this.col = col;
        this.worldPos = worldPos;
    }

    public Vec2D getWorldPos() {
        return worldPos;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
    
    public void setWalkable(boolean walkable) {
        walkable = true;
    }

    public boolean getWalkable() {
        return walkable;
    }

    public double getGCost() {
        return gCost;
    }

    public void setGCost(double gCost) {
        this.gCost = gCost;
    }

    public double getHCost() {
        return hCost;
    }

    public void setHCost(double hCost) {
        this.hCost = hCost;
    }

    public double getFCost() {
        fCost = gCost + hCost;
        return fCost;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getParent() {
        return parent;
    }
}