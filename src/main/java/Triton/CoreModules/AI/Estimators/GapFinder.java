package Triton.CoreModules.AI.Estimators;

import Triton.App;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Coordinates.Gridify;
import Triton.Misc.Math.Geometry.Line2D;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.Misc.ModulePubSubSystem.FieldPublisher;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Publisher;
import Triton.Misc.ModulePubSubSystem.Subscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import static Triton.Misc.Math.Coordinates.PerspectiveConverter.normAng;

public class GapFinder {

    private final Subscriber<HashMap<String, Integer>> fieldSizeSub;

    private final RobotList<Ally> fielders;
    private final RobotList<Foe> foes;
    private final Ball ball;

    private final Gridify grid;
    double worldWidth, worldLength;
    int  resolutionStepSize = 100;

    private double responseRange = 1000.0;
    private double interceptRange = 500.0;

    private int[] gridOrigin;
    private int width, height;

    private final Publisher<double[][]> pmfPub;
    private final Subscriber<double[][]> pmfSub;
    private final Publisher<ArrayList<Vec2D>> fielderPosListPub;
    private final Subscriber<ArrayList<Vec2D>> fielderPosListSub;
    private final Publisher<ArrayList<Vec2D>> foePosListPub;
    private final Subscriber<ArrayList<Vec2D>> foePosListSub;
    private final Publisher<Vec2D> ballPosPub;
    private final Subscriber<Vec2D> ballPosSub;

    public GapFinder(RobotList<Ally> fielders, RobotList<Foe> foes, Ball ball) {
        this.foes = foes;
        this.ball = ball;
        this.fielders = fielders;

        /* internal pub-sub */
        pmfPub = new FieldPublisher<>("GapFinder", "PDF", null);
        pmfSub = new FieldSubscriber<>("GapFinder", "PDF");

        fielderPosListPub = new FieldPublisher<>("GapFinder", "FielderPositions", null);
        fielderPosListSub = new FieldSubscriber<>("GapFinder", "FielderPositions");

        foePosListPub = new FieldPublisher<>("GapFinder", "FoePositions", null);
        foePosListSub = new FieldSubscriber<>("GapFinder", "FoePositions");

        ballPosPub = new FieldPublisher<>("GapFinder", "BallPosition", null);
        ballPosSub = new FieldSubscriber<>("GapFinder", "BallPosition");


        /* external pub-sub */
        fieldSizeSub = new FieldSubscriber<>("geometry", "fieldSize");

        try {
            fieldSizeSub.subscribe(1000);

            fielderPosListSub.subscribe(1000);
            foePosListSub.subscribe(1000);
            ballPosSub.subscribe(1000);
            pmfSub.subscribe(1000);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        HashMap<String, Integer> fieldSize = fieldSizeSub.getMsg();
        while (fieldSize == null || fieldSize.get("fieldLength") == 0 || fieldSize.get("fieldWidth") == 0);
        worldWidth = fieldSize.get("fieldWidth");
        worldLength = fieldSize.get("fieldLength");
        grid = new Gridify(new Vec2D(resolutionStepSize, resolutionStepSize),
                            new Vec2D(-worldWidth / 2, -worldLength / 2), false, false);

        gridOrigin = grid.fromPos(new Vec2D(-worldWidth / 2, -worldLength / 2));
        width = grid.numCols(worldWidth) + 1;
        height = grid.numRows(worldLength) + 1;

        App.threadPool.submit(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    calcProb();
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }

    public void setResponseRange(double responseRange) {
        this.responseRange = responseRange;
    }


    public double getProb(double[][] pmf, Vec2D pos) {
        int[] idx = grid.fromPos(pos);
        if(pmf == null) return 0.0;
        return pmf[idx[0]][idx[1]];
    }

    public double[][] getPMF() {
        ArrayList<Vec2D> fielderPosList = new ArrayList<>();
        for (Ally fielder : fielders) {
            fielderPosList.add(fielder.getPos());
        }
        ArrayList<Vec2D> foePosList = new ArrayList<>();
        for (Foe foe : foes) {
            foePosList.add(foe.getPos());
        }
        Vec2D ballPos = ball.getPos();

        fielderPosListPub.publish(fielderPosList);
        foePosListPub.publish(foePosList);
        ballPosPub.publish(ballPos);

        return pmfSub.getMsg();
    }



    /* calculate the pmf of optimal gap region for passing and attacking,
     * the pmf are indexed by gridIdx instead of coordinates
     *   */
    private void calcProb() {

        // long t0 = System.currentTimeMillis();

        double[][] pmf = new double[width][height];

        ArrayList<Vec2D> fielderPosList = fielderPosListSub.getMsg();
        ArrayList<Vec2D> foePosList = foePosListSub.getMsg();
        Vec2D ballPos = ballPosSub.getMsg();

        if(fielderPosList == null || foePosList == null || ballPos == null) {
            return;
        }

        for (int gridX = gridOrigin[0]; gridX < width; gridX++) {
            for (int gridY = gridOrigin[1]; gridY < height; gridY++) {
                Vec2D pos = grid.fromInd(gridX, gridY);
                double prob = 1.0;

                /* away from foe bots */
                double minDist = Double.MAX_VALUE;
                for (Vec2D foePos : foePosList) {
                    double dist = pos.sub(foePos).mag();
                    if (dist < minDist) {
                        minDist = dist;
                    }
                }
                double ratio = minDist / responseRange;
                if(ratio < 1.0) {
                    prob *= ratio;
                }

                /* make it harder to be intercept between foe and ball */
                double distFoeToIntercept = Double.MAX_VALUE;
                Vec2D nearestFoePos = null;
                /* find nearest foe dist to the potential passing line */
                for (Vec2D foePos : foePosList) {
                    // enforce correct direction
                    double dist = foePos.distToLine(new Line2D(pos, ballPos));
                    if (dist < distFoeToIntercept) {
                        distFoeToIntercept = dist;
                        nearestFoePos = foePos;
                    }
                }

                if(nearestFoePos != null &&
                        Math.abs(normAng(nearestFoePos.sub(ballPos).toPlayerAngle()
                                - pos.sub(ballPos).toPlayerAngle())) < 60)
                {
                    /* if greater, prob should be 100% for assuming foe can't intercept (indeed an assumption to make implementation simpler)*/
                    if (distFoeToIntercept < interceptRange) {
                        prob *= distFoeToIntercept / interceptRange;
                    }
                }


                pmf[gridX][gridY] = prob;
            }
        }

        /* mask forbidden and unlikely regions */
        // To-do

        pmfPub.publish(pmf);

        // System.out.println(System.currentTimeMillis() - t0);
    }


}
