package Triton.Computation.ThetaStar;

import java.util.*;

import Triton.Shape.Circle2D;
import Triton.Shape.Vec2D;

public class Pathfinder {

    public class NodeComparator implements Comparator<Node> {
        public int compare(Node a, Node b) {
            if (a.getFCost() == b.getFCost()) {
                return (int) (a.getHCost() - b.getHCost());
            } else {
                return (int) (a.getFCost() - b.getFCost());
            }
        }
    }

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
            closedSet.add(currentNode);

            if (currentNode == targetNode)
                return retracePath(startNode, targetNode);

            for (Node neighbor : grid.getNeighbors(currentNode)) {
                if (!neighbor.getWalkable() || closedSet.contains(neighbor))
                    continue;

                Node previousNode = currentNode;
                if (currentNode != startNode && grid.checkLineOfSight(currentNode.getParent(), neighbor))
                    previousNode = currentNode.getParent();

                double newMovementCostToNeighbor = previousNode .getGCost() + getDist(previousNode, neighbor);
                if (newMovementCostToNeighbor < neighbor.getGCost() || !openSet.contains(neighbor)) {
                    neighbor.setGCost(newMovementCostToNeighbor);
                    neighbor.setHCost(getDist(neighbor, targetNode));
                    neighbor.setParent(previousNode);
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
        worldPath.add(currentNode.getWorldPos());
        Collections.reverse(worldPath);
        return worldPath;
    }

    private double getDist(Node nodeA, Node nodeB) {
        return Vec2D.dist(nodeA.getWorldPos(), nodeB.getWorldPos());
    }

    public void updateGrid(ArrayList<Circle2D> obstacles) {
        grid.updateGrid(obstacles);
    }

    public Grid getGrid() {
        return grid;
    }
}