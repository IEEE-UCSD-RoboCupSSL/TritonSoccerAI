package Triton.Computation.AStar;

import java.util.ArrayList;

import Triton.Config.ObjectConfig;
import Triton.Shape.Circle2D;
import Triton.Shape.Rect2D;
import Triton.Shape.Vec2D;

public class Grid {
    private double worldSizeX;
    private double worldSizeY;
    private Node[][] nodes;
    private double nodeRadius;
    private double nodeDiameter;
    private int numRows, numCols;
    
    public Grid(double worldSizeX, double worldSizeY, double nodeRadius) {
        this.worldSizeX = worldSizeX;
        this.worldSizeY = worldSizeY;
        this.nodeRadius = nodeRadius;
        nodeDiameter = nodeRadius * 2;
        numCols = (int) (worldSizeX / nodeDiameter);
        numRows = (int) (worldSizeY / nodeDiameter);
        createGrid();
    }

    private Vec2D gridToWorld(int row, int col) {
        return new Vec2D(col * nodeDiameter + nodeRadius - worldSizeX / 2, worldSizeY - (row * nodeDiameter + nodeRadius) - worldSizeY / 2);
    }

    private void createGrid() {
        nodes = new Node[numRows][numCols];
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                Vec2D worldPos = gridToWorld(row, col);
                nodes[row][col] = new Node(worldPos);
            }
        }
    }

    public void updateGrid(ArrayList<Circle2D> obstacles) {
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                boolean walkable = true;
                for (Circle2D obstacle : obstacles) {
                    Vec2D center = obstacle.center;
                    double radius = (obstacle.radius + nodeDiameter + ObjectConfig.ROBOT_RADIUS + 10);
                    if (Vec2D.dist(nodes[row][col].getWorldPos(), center) < radius) { 
                        walkable = false;
                    }
                }
                nodes[row][col].setWalkable(walkable);
            }
        }
    }

    public Node[][] getNodes() {
        return nodes;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }
}