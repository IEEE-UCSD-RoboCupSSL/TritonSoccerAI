package Triton.Computation.PathFinder.JPS;

import Triton.Computation.Gridify;
import Triton.Computation.PathFinder.PathFinder;
import Triton.Config.PathfinderConfig;
import Triton.Shape.Circle2D;
import Triton.Shape.Line2D;
import Triton.Shape.Vec2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Future;

public class JPSPathFinder extends PathFinder {

    private final JPS<Node> jps;
    private final List<List<Node>> nodeList = new ArrayList<>();
    public Gridify convert;
    private final int numRows, numCols;
    private final double worldSizeX, worldSizeY;
    private final List<Node> lastObstacles = new ArrayList<>();

    public JPSPathFinder(double worldSizeX, double worldSizeY) {
        super("JPS");
        this.worldSizeX = worldSizeX;
        this.worldSizeY = worldSizeY;

        convert = new Gridify(
                new Vec2D(PathfinderConfig.NODE_DIAMETER, PathfinderConfig.NODE_DIAMETER),
                new Vec2D(PathfinderConfig.NODE_RADIUS - worldSizeX / 2,
                        PathfinderConfig.NODE_RADIUS - worldSizeY / 2),
                false, true);

        numCols = convert.numCols(worldSizeX);
        numRows = convert.numRows(worldSizeY);

        for (int row = 0; row < numRows; row++) {
            List<Node> nodes = new ArrayList<>();
            for (int col = 0; col < numCols; col++) {
                nodes.add(new Node(col, row));
            }
            nodeList.add(nodes);
        }

        jps = JPS.JPSFactory.getJPS(new Graph<>(nodeList), Graph.Diagonal.NO_OBSTACLES);
    }

    /* Set the four boundaries as not walkable */
    public void setBoundary() {
        // Four boundaries
        double up     =  worldSizeY / 2 - PathfinderConfig.SAFE_DIST;
        double bottom = -worldSizeY / 2 + PathfinderConfig.SAFE_DIST;
        double left   = -worldSizeX / 2 + PathfinderConfig.SAFE_DIST;
        double right  =  worldSizeX / 2 - PathfinderConfig.SAFE_DIST;

        // Upper-left and Bottom-right corners
        int[] ul = convert.fromPos(new Vec2D(left, up));
        int[] br = convert.fromPos(new Vec2D(right, bottom));

        // Set the boundaries as not walkable
        for (int col = ul[0]; col <= br[0]; col++) {
            nodeList.get(ul[1]).get(col).setWalkable(false);
            nodeList.get(br[1]).get(col).setWalkable(false);
        }
        for (int row = ul[1]; row <= br[1]; row++) {
            nodeList.get(row).get(ul[0]).setWalkable(false);
            nodeList.get(row).get(br[0]).setWalkable(false);
        }
    }

    /* Set the robot surroundings as not walkable */
    public void setObstacles(ArrayList<Circle2D> obstacles) {
        setBoundary();

        // Free last obstacles
        for (Node node : lastObstacles) {
            node.setWalkable(true);
        }
        lastObstacles.clear();
        
        for (Circle2D obstacle : obstacles) {
            double x = obstacle.center.x;
            double y = obstacle.center.y;
            double r = obstacle.radius + PathfinderConfig.SAFE_DIST;

            // Upper-left and Bottom-right corners
            int[] ul = convert.fromPos(new Vec2D(x - r, y + r));
            int[] br = convert.fromPos(new Vec2D(x + r, y - r));
            // Center
            int[] ce = convert.fromPos(new Vec2D(x, y));

            // Set the surroundings as not walkable
            for (int col = ul[0]; col <= br[0]; col++) {
                for (int row = ul[1]; row <= br[1]; row++) {
                    double dist = Math.sqrt(Math.pow((col - ce[0]), 2) + Math.pow((row - ce[1]), 2));
                    if (dist * PathfinderConfig.NODE_DIAMETER < r) {
                        nodeList.get(row).get(col).setWalkable(false);
                        lastObstacles.add(nodeList.get(row).get(col));
                    }
                }
            }
        }
    }

    public void paintObstacles(Graphics2D g2d, Gridify convert) {
        for (int col = 0; col < numCols; col++) {
            for (int row = 0; row < numRows; row++) {
                Node node = nodeList.get(row).get(col);
                Vec2D worldPos = this.convert.fromInd(node.getX(), node.getY());
                int[] displayPos = convert.fromPos(worldPos);
                if (!node.isWalkable()) {
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(5));
                    g2d.drawLine(displayPos[0], displayPos[1], displayPos[0], displayPos[1]);
                }
            }
        }
    }

    public ArrayList<Vec2D> findPath(Vec2D startPos, Vec2D targetPos) {
        int[] startIdx  = constrain(convert.fromPos(startPos));
        int[] targetIdx = constrain(convert.fromPos(targetPos));
        Node start  = nodeList.get(startIdx[1]).get(startIdx[0]);
        Node target = nodeList.get(targetIdx[1]).get(targetIdx[0]);

        Future<Queue<Node>> futurePath = jps.findPath(start, target);
        try {
            return toVec2DPath(futurePath.get());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private int[] constrain(int[] idx) {
        idx[0] = Math.min(Math.max(idx[0], 0), numCols);
        idx[1] = Math.min(Math.max(idx[1], 0), numRows);
        return idx;
    }

    private ArrayList<Vec2D> toVec2DPath(Queue<Node> path) {
        if (path == null || path.isEmpty()) return null;

        ArrayList<Vec2D> vec_path = new ArrayList<>();
        Node start = path.peek(); // start of the any-angle segment
        Node end = path.peek();   // end of the any-angle segment
        Node node; // current node
        vec_path.add(convert.fromInd(start.getX(), start.getY()));

        do {
            node = path.poll();
            if (!checkLineOfSight(start, node)) {
                vec_path.add(convert.fromInd(end.getX(), end.getY()));
                start = end;
            }
            end = node;
        } while (!path.isEmpty());

        if (node != start) {
            vec_path.add(convert.fromInd(node.getX(), node.getY()));
        }
        return vec_path;
    }

    public boolean checkLineOfSight(Node nodeA, Node nodeB) {
        Vec2D pointA = convert.fromInd(nodeA.getX(), nodeA.getY());
        Vec2D pointB = convert.fromInd(nodeB.getX(), nodeB.getY());
        Line2D line = new Line2D(pointA, pointB);
        double totalDist = line.length();
        int moveCount = (int) totalDist / PathfinderConfig.NODE_RADIUS;
        Vec2D dir = line.getDir();
        Vec2D moveAdd = dir.mult(PathfinderConfig.NODE_RADIUS);

        Vec2D currentPos = new Vec2D(pointA);
        for (int i = 0; i < moveCount; i++) {
            currentPos = currentPos.add(moveAdd);
            int[] idx = convert.fromPos(currentPos);
            Node currentNode = nodeList.get(idx[1]).get(idx[0]);
            if (!currentNode.isWalkable())
                return false;
        }
        return true;
    }

    public int getNumRows() { return numRows; }
    public int getNumCols() { return numCols; }
    public List<List<Node>> getNodeList() {
        return nodeList;
    }
}
