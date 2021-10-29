package triton.coreModules.ai.pathFinder.jumpPointSearch;

import triton.config.Config;
import triton.config.globalVariblesAndConstants.GvcPathfinder;
import triton.coreModules.ai.pathFinder.PathFinder;
import triton.misc.math.coordinates.Gridify;
import triton.misc.math.geometry.Circle2D;
import triton.misc.math.geometry.Line2D;
import triton.misc.math.linearAlgebra.Vec2D;
import triton.periphModules.display.JPSPathfinderDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Future;

import static triton.config.globalVariblesAndConstants.GvcGeometry.RIGHT_FIELD_RIGHT_PENALTY_STRETCH;

public class JPSPathFinder extends PathFinder {

    private final boolean keeper;
    private final JPS<Node> jps;
    private final List<List<Node>> nodeList = new ArrayList<>();
    private final Gridify convert;
    private final int numRows, numCols;
    private final double worldSizeX, worldSizeY;
    private final List<Node> lastObstacles = new ArrayList<>();
    private ArrayList<Vec2D> path = new ArrayList<>();
    private final Config config;
    private final int[] leftPenaltyUL, leftPenaltyBR, rightPenaltyUL, rightPenaltyBR;

    public JPSPathFinder(double worldSizeX, double worldSizeY, Config config, boolean keeper) {
        super("JPS");
        this.keeper = keeper;
        this.config = config;
        this.worldSizeX = worldSizeX + GvcPathfinder.BOUNDARY_EXTENSION * 2;
        this.worldSizeY = worldSizeY + GvcPathfinder.BOUNDARY_EXTENSION * 2;

        convert = new Gridify(
                new Vec2D(GvcPathfinder.NODE_DIAMETER, GvcPathfinder.NODE_DIAMETER),
                new Vec2D(GvcPathfinder.NODE_RADIUS - this.worldSizeX / 2,
                        GvcPathfinder.NODE_RADIUS - this.worldSizeY / 2),
                false, true);

        // Two penalty regions
        double y = RIGHT_FIELD_RIGHT_PENALTY_STRETCH.p2.x;
        double x = RIGHT_FIELD_RIGHT_PENALTY_STRETCH.p2.y;
        double wy = RIGHT_FIELD_RIGHT_PENALTY_STRETCH.p1.x;

        leftPenaltyUL = convert.fromPos(new Vec2D(-x, -y));
        leftPenaltyBR = convert.fromPos(new Vec2D(x, -wy));
        rightPenaltyUL = convert.fromPos(new Vec2D(-x, wy));
        rightPenaltyBR = convert.fromPos(new Vec2D(x, y));

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
        if (keeper) return;

        for (int col = leftPenaltyUL[0]; col < leftPenaltyBR[0]; col++) {
            for (int row = leftPenaltyUL[1]; row <= leftPenaltyBR[1]; row++) {
                nodeList.get(row).get(col).setWalkable(false);
            }
        }

        for (int col = rightPenaltyUL[0]; col < rightPenaltyBR[0]; col++) {
            for (int row = rightPenaltyUL[1]; row <= rightPenaltyBR[1]; row++) {
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
            if (!inPenalty(startIdx[1], startIdx[0])) {
                wayOut(startIdx[1], startIdx[0]);
            }
        }
        if (!target.isWalkable()) {
            if (!inPenalty(targetIdx[1], targetIdx[0])) {
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

    /* Check if a point is in penalty area */
    public boolean inPenalty(int row, int col) {
        return (row >= leftPenaltyUL[1] && row <= leftPenaltyBR[1] && col >= leftPenaltyUL[0] && col <= leftPenaltyBR[0])
        || (row >= rightPenaltyUL[1] && row <= rightPenaltyBR[1] && col >= rightPenaltyUL[0] && col <= rightPenaltyBR[0]);
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
