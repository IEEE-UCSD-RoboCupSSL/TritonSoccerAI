package Triton.CoreModules.AI.Estimators;

import Triton.App;
import Triton.CoreModules.Robot.RobotSnapshot;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.Misc.RWLockee;

import java.util.ArrayList;

public abstract class ProbFinder {

    public abstract double[][] getPMF();
    public abstract double getProb(double[][] pmf, Vec2D pos);
    public abstract ArrayList<Vec2D> getTopNMaxPos(int n);
    public abstract ArrayList<Vec2D> getTopNMaxPosWithClearance(int n, double interAllyClearance);
    protected abstract void calcProb();

    protected RWLockee<Vec2D> ballPosWrapper;
    protected ArrayList<RobotSnapshot> fielderSnaps = new ArrayList<>();
    protected ArrayList<RobotSnapshot> foeSnaps = new ArrayList<>();

    public void run() {
        App.threadPool.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
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
}
