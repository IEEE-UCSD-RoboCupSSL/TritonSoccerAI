package triton.coreModules.ai.pathFinder.thetaStar;

import triton.coreModules.ai.pathFinder.PathFinder;
import triton.misc.math.geometry.Circle2D;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.*;

public class ThetaStarPathFinder extends PathFinder {

    public Grid grid;

    public ThetaStarPathFinder(double worldSizeX, double worldSizeY) {
        super("θ* ");
        grid = new Grid(worldSizeX, worldSizeY);
    }

    public void setObstacles(ArrayList<Circle2D> obstacles) {
        grid.updateGrid(obstacles);
    }

    public ArrayList<Vec2D> findPath(Vec2D startPos, Vec2D targetPos) {
        Node startNode = grid.nodeFromWorldPos(startPos);
        Node targetNode = grid.nodeFromWorldPos(targetPos);

        PriorityQueue<Node> openSet = new PriorityQueue<>(1, new NodeComparator());
        HashSet<Node> closedSet = new HashSet<>();
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

                double newMovementCostToNeighbor = previousNode.getGCost() + getDist(previousNode, neighbor);
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
        ArrayList<Vec2D> worldPath = new ArrayList<>();
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

    public Grid getGrid() {
        return grid;
    }

    public static class NodeComparator implements Comparator<Node> {
        public int compare(Node a, Node b) {
            if (a.getFCost() == b.getFCost()) {
                return (int) (a.getHCost() - b.getHCost());
            } else {
                return (int) (a.getFCost() - b.getFCost());
            }
        }
    }
}