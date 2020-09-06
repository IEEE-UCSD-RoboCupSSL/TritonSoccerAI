package Triton.Computation.AStar;

import java.util.*;

import Triton.Shape.Circle2D;
import Triton.Shape.Vec2D;

public class Pathfinder {

    public class NodeComparator implements Comparator<Node> {
        public int compare(Node a, Node b) {
            if (a.getFCost() == b.getFCost()) {
                return a.getHCost() - b.getHCost();
            } else {
                return a.getFCost() - b.getFCost();
            }
        }
    }

    private static int PERP_DIST = 10;
    private static int DIAG_DIST = 14;
    public Grid grid;

    public Pathfinder(double worldSizeX, double worldSizeY) {
        grid = new Grid(worldSizeX, worldSizeY);
    }

    public ArrayList<Vec2D> findPath(Vec2D startPos, Vec2D targetPos) {
        Node startNode = grid.nodeFromWorldPos(startPos);
        Node targetNode = grid.nodeFromWorldPos(targetPos);

        PriorityQueue<Node> openSet = new PriorityQueue<Node>(1, new NodeComparator());
        HashSet<Node> closedSet = new HashSet<Node>();
        openSet.add(startNode);

        while (openSet.size() > 0) {
            Node currentNode = openSet.poll();

            openSet.remove(currentNode);
            closedSet.add(currentNode);

            if (currentNode == targetNode)
                return retracePath(startNode, targetNode);

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
        return null;
    }

    private ArrayList<Vec2D> retracePath(Node startNode, Node targetNode) {
        ArrayList<Vec2D> worldPath = new ArrayList<Vec2D>();
        Node currentNode = targetNode;

        while (currentNode != startNode) {
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
            return DIAG_DIST * distCol + PERP_DIST * (distRow - distCol);
        return DIAG_DIST * distRow + PERP_DIST * (distCol - distRow);
    }

    public void updateGrid(ArrayList<Circle2D> obstacles) {
        grid.updateGrid(obstacles);
    }

    public Grid getGrid() {
        return grid;
    }
}