package Triton.CoreModules.AI.TritonProbDijkstra.ComputableImpl;

import Triton.CoreModules.AI.TritonProbDijkstra.Computables.DijkCompute;
import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;
import Triton.CoreModules.Robot.RobotSnapshot;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.RWLockee;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

@Getter
@Setter
public class MockCompute implements DijkCompute {
    private final HashMap<PUAG.Node, Integer> nodeToIndexMap = new HashMap<>();
    private final double[][] probMatrix;
    private final double[][] angleMatrix;
    private final Vec2D[][] kickVecMatrix;
    private final Vec2D[][] passPointMatrix;
    private final Vec2D[][] recepPointMatrix;


    private PUAG graph;

    public MockCompute(PUAG graph) {
        Set<PUAG.Node> nodeSet = graph.getNodeSet();

        int index = 0;
        for (PUAG.Node node : nodeSet) {
            nodeToIndexMap.put(node, index++);
        }

        int workingSize = nodeToIndexMap.size();

        probMatrix = new double[workingSize][workingSize];
        angleMatrix = new double[workingSize][workingSize];
        kickVecMatrix = new Vec2D[workingSize][workingSize];
        passPointMatrix = new Vec2D[workingSize][workingSize];
        recepPointMatrix = new Vec2D[workingSize][workingSize];

        for (int i = 0; i < workingSize; i++) {
            for (int j = 0; j < workingSize; j++) {
                kickVecMatrix[i][j] = new Vec2D(0, 0);
                passPointMatrix[i][j] = new Vec2D(0, 0);
                recepPointMatrix[i][j] = new Vec2D(0, 0);
            }
        }
    }

    public int getIndexOfNode(PUAG.Node n){
        Integer integer = nodeToIndexMap.get(n);
        if(integer == null){
            return -1;
        }
        return integer;
    }

    public boolean setProb(PUAG.Node n1, PUAG.Node n2, double prob) {
        try {
            probMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)] = prob;
        } catch (IndexOutOfBoundsException e){
            return false;
        }
        return true;
    }

    public void setAllProb(double prob){
        for (int i = 0; i < probMatrix.length; i++) {
            for (int j = 0; j < probMatrix.length; j++) {
                probMatrix[i][j] = prob;
            }
        }
    }

    public void setAngle(PUAG.Node n1, PUAG.Node n2, double angle) {
        angleMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)] = angle;
    }

    public void setKickVec(PUAG.Node n1, PUAG.Node n2, Vec2D kickVec) {
        kickVecMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)] = kickVec;

    }

    public void setPasspoint(PUAG.Node n1, PUAG.Node n2, Vec2D passpoint) {
        passPointMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)] = passpoint;
    }

    public void setRecepPoint(PUAG.Node n1, PUAG.Node n2, Vec2D recepPoint) {
        recepPointMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)] = recepPoint;
    }

    @Override
    public double computeProb(PUAG.Node n1, PUAG.Node n2) {
        return probMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)];
    }

    @Override
    public double computeGoalProb(PUAG.Node n) {
        return 0.7;
    }

    @Override
    public double computeAngle(PUAG.Node n1, PUAG.Node n2) {
        return angleMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)];
    }

    @Override
    public Vec2D computeKickVec(PUAG.Node n1, PUAG.Node n2) {
        return kickVecMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)];
    }

    @Override
    public Vec2D computePasspoint(PUAG.Node n1, PUAG.Node n2) {
        return passPointMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)];
    }

    @Override
    public Vec2D computeRecepPoint(PUAG.Node n1, PUAG.Node n2) {
        return recepPointMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)];
    }

    @Override
    public Vec2D computeGoalCenter(PUAG.Node n) {
        return null;
    }

    @Override
    public void setSnapShots(ArrayList<RobotSnapshot> allySnaps, ArrayList<RobotSnapshot> foeSnaps, RWLockee<Vec2D> ballSnap) {

    }
}
