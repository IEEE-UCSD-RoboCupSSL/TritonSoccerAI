package Triton.Computation.AStar;

import java.util.ArrayList;

import Triton.Config.ObjectConfig;
import Triton.Shape.Circle2D;
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
        numCols = (int) Math.round(worldSizeX / nodeDiameter);
        numRows = (int) Math.round(worldSizeY / nodeDiameter);
        createGrid();
    }

    private Vec2D gridPosToWorldPos(int row, int col) {
        return new Vec2D(col * nodeDiameter + nodeRadius - worldSizeX / 2,
                -row * nodeDiameter - nodeRadius + worldSizeY / 2);
    }

    private int[] wordPosToGridPos(Vec2D worldPos) {
        int[] res = { (int) Math.round((worldSizeY / 2 - worldPos.y) / nodeDiameter - 0.5),
                (int) Math.round((worldPos.x + worldSizeX / 2 - nodeRadius) / nodeDiameter), };
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
                if (nodeWorldPos.x < -worldSizeX / 2 + nodeRadius + ObjectConfig.ROBOT_RADIUS
                        || nodeWorldPos.x > worldSizeX / 2 - nodeRadius - ObjectConfig.ROBOT_RADIUS
                        || nodeWorldPos.y < -worldSizeY / 2 + nodeRadius + ObjectConfig.ROBOT_RADIUS
                        || nodeWorldPos.y > worldSizeY / 2 - nodeRadius - ObjectConfig.ROBOT_RADIUS)
                    nodes[row][col].setWalkable(false);
                else {
                    for (Circle2D obstacle : obstacles) {
                        Vec2D center = obstacle.center;
                        double radius = (nodeRadius + obstacle.radius + ObjectConfig.ROBOT_RADIUS);
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