package Triton.Computation.JPS;

import Triton.Computation.Gridify;
import Triton.Config.PathfinderConfig;
import Triton.Shape.Circle2D;
import Triton.Shape.Vec2D;

import java.util.ArrayList;
import java.util.List;

public class PathFinder {

    private JPS<Node> jps;
    private List<List<Node>> nodeList;
    private Gridify convert;
    private int numRows, numCols;
    private double worldSizeX, worldSizeY;


    public PathFinder(double worldSizeX, double worldSizeY) {
        this.worldSizeX = worldSizeX;
        this.worldSizeY = worldSizeY;

        convert = new Gridify(
                new Vec2D(PathfinderConfig.NODE_DIAMETER, PathfinderConfig.NODE_DIAMETER),
                new Vec2D(PathfinderConfig.NODE_RADIUS - worldSizeX / 2,
                        PathfinderConfig.NODE_RADIUS - worldSizeY / 2),
                false, true);

        numCols = convert.numCols(worldSizeX);
        numRows = convert.numRows(worldSizeY);

        for (int col = 0; col < numCols; col++) {
            List<Node> column = new ArrayList<>();
            for (int row = 0; row < numRows; row++) {
                column.add(new Node(row, col));
            }
            nodeList.add(column);
        }

        jps = JPS.JPSFactory.getJPS(new Graph<>(nodeList), Graph.Diagonal.ALWAYS);
    }


    /* Set the four boundaries as not walkable */
    public void setBoundary() {
        // Four boundaries
        double up    =  worldSizeY / 2 - PathfinderConfig.SAFE_DIST;
        double bottom  = -worldSizeY / 2 + PathfinderConfig.SAFE_DIST;
        double left  = -worldSizeX / 2 + PathfinderConfig.SAFE_DIST;
        double right =  worldSizeX / 2 - PathfinderConfig.SAFE_DIST;

        // Upper-left and Bottom-right corners
        int[] ul = convert.fromPos(new Vec2D(left, up));
        int[] br = convert.fromPos(new Vec2D(right, bottom));

        // Set the boundaries as not walkable
        for (int col = ul[0]; col <= br[0]; col++) {
            nodeList.get(col).get(ul[1]).setWalkable(false);
            nodeList.get(col).get(br[1]).setWalkable(false);
        }
        for (int row = br[1]; row <= ul[1]; row++) {
            nodeList.get(ul[0]).get(row).setWalkable(false);
            nodeList.get(br[0]).get(row).setWalkable(false);
        }
    }


    /* Set the robot surroundings as not walkable */
    public void setObstacles(ArrayList<Circle2D> obstacles) {
        setBoundary();

        // Set the obstacles as not walkable
        for (Circle2D obstacle : obstacles) {
            double x = obstacle.center.x;
            double y = obstacle.center.y;
            double r = obstacle.radius + PathfinderConfig.SAFE_DIST;

            // Upper-left and Bottom-right corners
            int[] ul = convert.fromPos(new Vec2D(x - r, y + r));
            int[] br = convert.fromPos(new Vec2D(x + r, y - r));

            // Set the square as not walkable
            for (int col = ul[0]; col <= br[0]; col++) {
                for (int row = br[1]; row <= ul[1]; row++) {
                    nodeList.get(col).get(row).setWalkable(false);
                }
            }
        }
    }
}
