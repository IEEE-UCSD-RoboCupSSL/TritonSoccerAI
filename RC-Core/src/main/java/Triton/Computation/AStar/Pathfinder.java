package Triton.Computation.AStar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import Triton.Shape.Circle2D;
import Triton.Shape.Vec2D;

public class Pathfinder {
    public Grid grid;

    public Pathfinder(double worldSizeX, double worldSizeY, double nodeRadius) {
        grid = new Grid(worldSizeX, worldSizeY, nodeRadius);
    }

    public ArrayList<Vec2D> findPath(Vec2D startPos, Vec2D targetPos) {
        Node startNode = grid.nodeFromWorldPos(startPos);
        Node targetNode = grid.nodeFromWorldPos(targetPos);

        ArrayList<Node> openSet = new ArrayList<Node>();
        HashSet<Node> closedSet = new HashSet<Node>();
        openSet.add(startNode);

        while (openSet.size() > 0) {
            Node currentNode = null;
            for (Node node : openSet) {
                if (currentNode == null || node.getFCost() < currentNode.getFCost()
                        || node.getFCost() == currentNode.getFCost() && node.getHCost() < currentNode.getHCost()) {
                    currentNode = node;
                }
            }

            openSet.remove(currentNode);
            closedSet.add(currentNode);

            if (currentNode == targetNode) {
                return retracePath(startNode, targetNode);
            }

            for (Node neighbor : grid.getNeighbors(currentNode)) {
                if (!neighbor.getWalkable() || closedSet.contains(neighbor))
                    continue;
                
                int newMovementCostToNeighbor = currentNode.getGCost() + getDist(currentNode, neighbor);
                if (newMovementCostToNeighbor < neighbor.getGCost() || !openSet.contains(neighbor)) {
                    neighbor.setGCost(newMovementCostToNeighbor);
                    neighbor.setHCost(getDist(neighbor, targetNode));
                    neighbor.setParent(currentNode);

                    if (!openSet.contains(neighbor))
                        openSet.add(neighbor);
                }
            }
        }
        System.out.println("!!!");
        return null;
    }

    private ArrayList<Vec2D> retracePath(Node startNode, Node targetNode) {
        ArrayList<Vec2D> worldPath = new ArrayList<Vec2D>();
        ArrayList<Node> nodePath = new ArrayList<Node>();
        Node currentNode = targetNode;

        while (currentNode != startNode) {
            nodePath.add(currentNode);
            worldPath.add(currentNode.getWorldPos());
            currentNode = currentNode.getParent();
        }
        Collections.reverse(worldPath);
        return worldPath;
    }

    private int getDist(Node nodeA, Node nodeB) {
        int distRow = Math.abs(nodeA.getRow() - nodeB.getRow());
        int distCol = Math.abs(nodeA.getCol() - nodeB.getCol());

        if (distRow > distCol)
            return 14 * distCol + 10 * (distRow - distCol);
        return 14 * distRow + 10 * (distCol - distRow);
    }

    public void updateGrid(ArrayList<Circle2D> obstacles) {
        grid.updateGrid(obstacles);
    }

    public Grid getGrid() {
        return grid;
    }
}