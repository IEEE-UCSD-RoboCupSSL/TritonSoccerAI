package Triton.CoreModules.AI.Estimators;

import Triton.Misc.Math.Matrix.Vec2D;

import java.util.ArrayList;

public abstract class ProbFinder {

    public abstract double[][] getPMF();
    public abstract double getProb(double[][] pmf, Vec2D pos);
    public abstract ArrayList<Vec2D> getTopNMaxPos(int n);
    public abstract ArrayList<Vec2D> getTopNMaxPosWithClearance(int n, double interAllyClearance);
}
