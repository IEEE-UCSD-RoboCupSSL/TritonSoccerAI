package Triton.Computation.AStar;

import java.util.ArrayList;

import Triton.Config.ObjectConfig;
import Triton.Config.PathfinderConfig;
import Triton.Shape.Circle2D;
import Triton.Shape.Vec2D;

public class Grid {
    private double worldSizeX;
    private double worldSizeY;
    private Node[][] nodes;
    private int numRows, numCols;

    public Grid(double worldSizeX, double worldSizeY) {
        this.worldSizeX = worldSizeX;
        this.worldSizeY = worldSizeY;
        numCols = (int) Math.round(worldSizeX / PathfinderConfig.NODE_DIAMETER);
        numRows = (int) Math.round(worldSizeY / PathfinderConfig.NODE_DIAMETER);
        createGrid();
    }

    private Vec2D gridPosToWorldPos(int row, int col) {
        return new Vec2D(col * PathfinderConfig.NODE_DIAMETER + PathfinderConfig.NODE_RADIUS - worldSizeX / 2,
                -row * PathfinderConfig.NODE_DIAMETER - PathfinderConfig.NODE_RADIUS + worldSizeY / 2);
    }

    private int[] wordPosToGridPos(Vec2D worldPos) {
        int[] res = { (int) Math.round((worldSizeY / 2 - worldPos.y) / PathfinderConfig.NODE_DIAMETER - 0.5),
                (int) Math.round((worldPos.x + worldSizeX / 2 - PathfinderConfig.NODE_RADIUS)
                        / PathfinderConfig.NODE_DIAMETER), };
        return res;
    }

    public Node nodeFromWorldPos(Vec2D worldPos) {
        int[] gridPos = wordPosToGridPos(worldPos);
        return nodes[gridPos[0]][gridPos[1]];
    }

    private void createGrid() {
        nodes = new Node[numRows][numCols];
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                Vec2D worldPos = gridPosToWorldPos(row, col);
                nodes[row][col] = new Node(worldPos, row, col);
            }
        }
    }

    public void updateGrid(ArrayList<Circle2D> obstacles) {
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                Vec2D nodeWorldPos = nodes[row][col].getWorldPos();
                nodes[row][col].setWalkable(true);
                if (nodeWorldPos.x < -worldSizeX / 2 + PathfinderConfig.NODE_RADIUS + ObjectConfig.ROBOT_RADIUS
                        + PathfinderConfig.SAFETY_DIST
                        || nodeWorldPos.x > worldSizeX / 2 - PathfinderConfig.NODE_RADIUS - ObjectConfig.ROBOT_RADIUS
                                - PathfinderConfig.SAFETY_DIST
                        || nodeWorldPos.y < -worldSizeY / 2 + PathfinderConfig.NODE_RADIUS + ObjectConfig.ROBOT_RADIUS
                                + PathfinderConfig.SAFETY_DIST
                        || nodeWorldPos.y > worldSizeY / 2 - PathfinderConfig.NODE_RADIUS - ObjectConfig.ROBOT_RADIUS
                                - PathfinderConfig.SAFETY_DIST) {
                    nodes[row][col].setWalkable(false);
                } else {
                    for (Circle2D obstacle : obstacles) {
                        Vec2D center = obstacle.center;
                        double radius = (PathfinderConfig.NODE_RADIUS + obstacle.radius + ObjectConfig.ROBOT_RADIUS);
                        if (Vec2D.dist(nodeWorldPos, center) < radius) {
                            nodes[row][col].setWalkable(false);
                            break;
                        }
                    }
                }
            }
        }
    }

    public ArrayList<Node> getNeighbors(Node node) {
        ArrayList<Node> neighbors = new ArrayList<Node>();
        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int colOffset = -1; colOffset <= 1; colOffset++) {
                if (rowOffset == 0 && colOffset == 0)
                    continue;

                int checkRow = node.getRow() + rowOffset;
                int checkCol = node.getCol() + colOffset;

                if (checkRow >= 0 && checkRow < numRows && checkCol >= 0 && checkCol < numCols) {
                    neighbors.add(nodes[checkRow][checkCol]);
                }
            }
        }
        return neighbors;
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