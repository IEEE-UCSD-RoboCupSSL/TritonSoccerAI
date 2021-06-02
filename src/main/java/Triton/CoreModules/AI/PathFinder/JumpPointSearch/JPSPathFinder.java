package Triton.CoreModules.AI.PathFinder.JumpPointSearch;

import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcPathfinder;
import Triton.CoreModules.AI.PathFinder.PathFinder;
import Triton.Misc.Math.Coordinates.Gridify;
import Triton.Misc.Math.Geometry.Circle2D;
import Triton.Misc.Math.Geometry.Line2D;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.PeriphModules.Display.JPSPathfinderDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Future;

public class JPSPathFinder extends PathFinder {

    private final JPS<Node> jps;
    private final List<List<Node>> nodeList = new ArrayList<>();
    private final Gridify convert;
    private final int numRows, numCols;
    private final double worldSizeX, worldSizeY;
    private final List<Node> lastObstacles = new ArrayList<>();
    private final int[] ul;
    private final int[] br;
    private ArrayList<Vec2D> path = new ArrayList<>();
    private final Config config;

    public JPSPathFinder(double worldSizeX, double worldSizeY, Config config) {
        super("JPS");
        this.config = config;
        this.worldSizeX = worldSizeX + GvcPathfinder.BOUNDARY_EXTENSION * 2;
        this.worldSizeY = worldSizeY + GvcPathfinder.BOUNDARY_EXTENSION * 2;

        convert = new Gridify(
                new Vec2D(GvcPathfinder.NODE_DIAMETER, GvcPathfinder.NODE_DIAMETER),
                new Vec2D(GvcPathfinder.NODE_RADIUS - this.worldSizeX / 2,
                        GvcPathfinder.NODE_RADIUS - this.worldSizeY / 2),
                false, true);

        // Upper-left and Bottom-right corners
        ul = convert.fromPos(new Vec2D(-worldSizeX / 2, worldSizeY / 2));
        br = convert.fromPos(new Vec2D(worldSizeX / 2, -worldSizeY / 2));

        numCols = convert.numCols(this.worldSizeX);
        numRows = convert.numRows(this.worldSizeY);

        for (int row = 0; row < numRows; row++) {
            List<Node> nodes = new ArrayList<>();
            for (int col = 0; col < numCols; col++) {
                nodes.add(new Node(col, row));
            }
            nodeList.add(nodes);
        }

        jps = JPS.JPSFactory.getJPS(new Graph<>(nodeList), Graph.Diagonal.NO_OBSTACLES);
    }

    /* Set the robot surroundings as not walkable */
    public void setObstacles(ArrayList<Circle2D> obstacles) {
        setBound();

        // Free last obstacles
        for (Node node : lastObstacles) {
            node.setWalkable(true);
        }
        lastObstacles.clear();

        for (Circle2D obstacle : obstacles) {
            double x = obstacle.center.x;
            double y = obstacle.center.y;
            double r = obstacle.radius + GvcPathfinder.SAFE_DIST;

            // Upper-left and Bottom-right corners
            int[] ul = convert.fromPos(new Vec2D(x - r, y + r));
            int[] br = convert.fromPos(new Vec2D(x + r, y - r));
            // Center
            int[] ce = convert.fromPos(new Vec2D(x, y));

            // Set the surroundings as not walkable
            for (int col = ul[0]; col <= br[0]; col++) {
                for (int row = ul[1]; row <= br[1]; row++) {
                    double dist = Math.sqrt(Math.pow((col - ce[0]), 2) + Math.pow((row - ce[1]), 2));
                    if (dist * GvcPathfinder.NODE_DIAMETER < r) {
                        nodeList.get(row).get(col).setWalkable(false);
                        lastObstacles.add(nodeList.get(row).get(col));
                    }
                }
            }
        }
    }

    /* Set area outside the boundaries as not walkable */
    public void setBound() {
        for (int col = 0; col < nodeList.get(0).size(); col++) {
            for (int row = 0; row <= ul[1]; row++) {
                nodeList.get(row).get(col).setWalkable(false);
            }
            for (int row = br[1]; row < nodeList.size(); row++) {
                nodeList.get(row).get(col).setWalkable(false);
            }
        }

        for (int row = ul[1]; row <= br[1]; row++) {
            for (int col = 0; col <= ul[0]; col++) {
                nodeList.get(row).get(col).setWalkable(false);
            }
            for (int col = br[0]; col < nodeList.get(0).size(); col++) {
                nodeList.get(row).get(col).setWalkable(false);
            }
        }
    }

    public ArrayList<Vec2D> findPath(Vec2D startPos, Vec2D targetPos) {
        int[] startIdx = constrain(convert.fromPos(startPos));
        int[] targetIdx = constrain(convert.fromPos(targetPos));

        Node start, target;
        try {
            start = nodeList.get(startIdx[1]).get(startIdx[0]);
            target = nodeList.get(targetIdx[1]).get(targetIdx[0]);
        } catch (IndexOutOfBoundsException e) {
            return nullPath(startPos);
        }

        // find ways out
        if (!start.isWalkable()) {
            wayOut(startIdx[1], startIdx[0]);
        }
        if (!target.isWalkable()) {
            if (!isOutOfBound(targetIdx[1], targetIdx[0])) { // Do nothing if ball is outside the field
                wayOut(targetIdx[1], targetIdx[0]);
            }
        }

        Future<Queue<Node>> futurePath = jps.findPath(start, target);
        try {
            Queue<Node> path = futurePath.get();
            this.path = toVec2DPath(path);
            return this.path;
        } catch (Exception e) {
            return nullPath(startPos);
        }
    }

    private int[] constrain(int[] idx) {
        idx[0] = Math.min(Math.max(idx[0], 0), numCols);
        idx[1] = Math.min(Math.max(idx[1], 0), numRows);
        return idx;
    }

    private ArrayList<Vec2D> nullPath(Vec2D startPos) {
        ArrayList<Vec2D> empty = new ArrayList<>();
        empty.add(startPos);
        empty.add(startPos);
        if (path != null) {
            System.out.println("No valid path found; empty path returned");
            path = null;
        }
        return empty;
    }

    /*
     * if start/target node is not walkable, BFS a way out
     */
    public void wayOut(int row, int col) {
        ArrayList<Node>[] paths = new ArrayList[8];
        for (int i = 0; i < 8; i++) {
            paths[i] = new ArrayList<>();
        }
        int searchBound = (int) Math.max(GvcPathfinder.SAFE_DIST, GvcPathfinder.BOUNDARY_EXTENSION)
                / GvcPathfinder.NODE_RADIUS;

        for (int i = 0; i <= searchBound; i++) {
            int arrInd = 0;
            for (int d_row = -1; d_row <= 1; d_row++) {
                for (int d_col = -1; d_col <= 1; d_col++) {
                    if (d_row == 0 && d_col == 0) continue;
                    try {
                        Node node = nodeList.get(d_row * i + row).get(d_col * i + col);
                        if (node.isWalkable()) {
                            for (Node n : paths[arrInd]) {
                                n.setWalkable(true); // clear the way
                            }
                            return;
                        } else {
                            paths[arrInd].add(node);
                            if (d_row * d_col != 0) { // diagonal entry
                                // also unblock the node above
                                paths[arrInd].add(nodeList.get(d_row * i + row + 1).get(d_col * i + col));
                            }
                        }
                    } catch (IndexOutOfBoundsException e) {
                        // Search out of bound, do nothing
                    }
                    arrInd++;
                }
            }
        }
    }

    /* Check if a point is in area outside the boundary */
    public boolean isOutOfBound(int row, int col) {
        return row <= ul[1] || row >= br[1] || col <= ul[0] || col >= br[0];
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
        int moveCount = (int) totalDist / GvcPathfinder.NODE_RADIUS;
        Vec2D dir = line.getDir();
        Vec2D moveAdd = dir.scale(GvcPathfinder.NODE_RADIUS);

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

    public void display() {
        new JPSPathfinderDisplay(this, config);
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public ArrayList<Vec2D> getPath() {
        return path;
    }

    public Gridify getConvert() {
        return convert;
    }

    public List<List<Node>> getNodeList() {
        return nodeList;
    }
}
