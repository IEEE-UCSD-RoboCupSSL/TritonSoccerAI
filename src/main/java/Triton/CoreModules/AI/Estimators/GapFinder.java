package Triton.CoreModules.AI.Estimators;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Coordinates.Gridify;
import Triton.Misc.Math.Geometry.Rect2D;
import Triton.Misc.Math.Matrix.Vec2D;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;

import java.util.ArrayList;

public class GapFinder {

    private final RobotList<Ally> fielders;
    private final RobotList<Foe> foes;
    private final Ball ball;

    private final Gridify grid;

    private double foeVariance = 100.0;

    public GapFinder(RobotList<Ally> fielders, RobotList<Foe> foes, Ball ball, Vec2D stepSize, Vec2D offset) {
        this.foes = foes;
        this.ball = ball;
        this.fielders = fielders;
        grid = new Gridify(stepSize, offset, false, false);
    }

    public void setFoeVariance(double variance) {
        foeVariance = variance;
    }


    public double getDensity(double[][] pdf, Vec2D pos) {
        int[] idx = grid.fromPos(pos);
        return pdf[idx[0]][idx[1]];
    }


    /* return the pdf of optimal gap region for passing and attacking,
     * the pdf returned are indexed by gridIdx instead of coordinates
     *   */
    public double[][] getProb(Rect2D region) {
        ArrayList<MultivariateNormalDistribution> normalList = new ArrayList<>();
        for (Foe foe : foes) {
            MultivariateNormalDistribution normal =
                    new MultivariateNormalDistribution(foe.getPos().toDoubleArray(), new double[][]{
                            new double[]{foeVariance, 0},
                            new double[]{0, foeVariance}}
                    );
            normalList.add(normal);
        }

        int[] gridPos = grid.fromPos(region.anchor);
        int width = grid.numCols(region.width) + 1;
        int height = grid.numRows(region.height) + 1;
        double[][] pdf = new double[width][height];

        ArrayList<Vec2D> fielderPosList = new ArrayList<>();
        for (int i = 0; i < fielders.size(); i++) {
            fielderPosList.add(fielders.get(i).getPos());
        }

        ArrayList<Vec2D> foePosList = new ArrayList<>();
        for (int i = 0; i < foes.size(); i++) {
            foePosList.add(foes.get(i).getPos());
        }

        Vec2D ballPos = ball.getPos();

        for (int gridX = gridPos[0]; gridX < width; gridX++) {
            for (int gridY = gridPos[1]; gridY < height; gridY++) {
                Vec2D pos = grid.fromInd(gridX, gridY);
                double prob = 1.0;
                /* away from foe bots */
                for (MultivariateNormalDistribution normal : normalList) {
                    prob *= (1 - normal.density(new double[]{pos.x, pos.y}));
                }
                prob *= 1; // To-do: make this a tunable coefficient/parameter

//                /* make it harder to be intercept between every pair of ally bots */
//                for (Vec2D fielderPos : fielderPosList) {
//                    double distFoeToIntercept = Double.MAX_VALUE;
//                    /* find nearest foe dist to the potential passing line */
//                    for (Vec2D foePos : foePosList) {
//                        double dist = foePos.distToLine(new Line2D(pos, fielderPos));
//                        if (dist < distFoeToIntercept) {
//                            distFoeToIntercept = dist;
//                        }
//                    }
//                    /* if greater, prob should be 100% for assuming foe can't intercept (indeed an assumption to make implementation simpler)*/
//                    if (distFoeToIntercept < foeVariance) {
//                        prob *= distFoeToIntercept / foeVariance;
//                    }
//                }
//                prob *= 1; // To-do: make this a tunable coefficient/parameter
//
//                /* make it harder to be intercept between this grid pos and ball pos */
//                double distFoeToIntercept = Double.MAX_VALUE;
//                /* find nearest foe dist to the potential passing line */
//                for (Vec2D foePos : foePosList) {
//                    double dist = foePos.distToLine(new Line2D(pos, ballPos));
//                    if (dist < distFoeToIntercept) {
//                        distFoeToIntercept = dist;
//                    }
//                }
//                /* if greater, prob should be 100% for assuming foe can't intercept (indeed an assumption to make implementation simpler)*/
//                if (distFoeToIntercept < foeVariance) {
//                    prob *= distFoeToIntercept / foeVariance;
//                }
//                prob *= 1; // To-do: make this a tunable coefficient/parameter
                pdf[gridX][gridY] = prob;
            }
        }

        /* mask forbidden and unlikely regions */
        // To-do

        return pdf;
    }


    public void display() {

    }
}
