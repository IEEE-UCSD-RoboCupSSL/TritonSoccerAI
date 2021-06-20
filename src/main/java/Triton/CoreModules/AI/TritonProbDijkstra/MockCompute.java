package Triton.CoreModules.AI.TritonProbDijkstra;

import Triton.Misc.Math.LinearAlgebra.Vec2D;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Set;

@Getter
@Setter
public class MockCompute implements DijkCompute {
    private final HashMap<PUAG.Node, HashMap<PUAG.Node, Double>> probMatrix = new HashMap<>();
    private final HashMap<PUAG.Node, HashMap<PUAG.Node, Double>> angleMatrix = new HashMap<>();
    private final HashMap<PUAG.Node, HashMap<PUAG.Node, Vec2D>> kickVecMatrix = new HashMap<>();
    private final HashMap<PUAG.Node, HashMap<PUAG.Node, Vec2D>> passpointMatrix = new HashMap<>();
    private final HashMap<PUAG.Node, HashMap<PUAG.Node, Vec2D>> recepPointMatrix = new HashMap<>();


    private PUAG graph;

    public MockCompute(PUAG graph) {
        Set<PUAG.Node> nodeSet = graph.getNodeSet();

        for (PUAG.Node node : nodeSet) {
            HashMap<PUAG.Node, Double> neighborProbMap = new HashMap<>();
            for (PUAG.Node adjacentNode : graph.getAdjacentNodes(node)) {
                neighborProbMap.put(adjacentNode, 0.0);
            }
            probMatrix.put(node, neighborProbMap);
        }
    }

    public boolean setProb(PUAG.Node n1, PUAG.Node n2, Double prob) {
        try {
            probMatrix.get(n1).put(n2, prob);
        } catch (NullPointerException e){
            return false;
        }
        return true;
    }

    public boolean setAngle(PUAG.Node n1, PUAG.Node n2, Double angle) {
        try {
            angleMatrix.get(n1).put(n2, angle);
        } catch (NullPointerException e){
            return false;
        }
        return true;
    }

    public boolean setKickVec(PUAG.Node n1, PUAG.Node n2, Vec2D kickVec) {
        try {
            kickVecMatrix.get(n1).put(n2, kickVec);
        } catch (NullPointerException e){
            return false;
        }
        return true;
    }

    public boolean setPasspoint(PUAG.Node n1, PUAG.Node n2, Vec2D passpoint) {
        try {
            passpointMatrix.get(n1).put(n2, passpoint);
        } catch (NullPointerException e){
            return false;
        }
        return true;
    }

    public boolean setRecepPoint(PUAG.Node n1, PUAG.Node n2, Vec2D recepPoint) {
        try {
            recepPointMatrix.get(n1).put(n2, recepPoint);
        } catch (NullPointerException e){
            return false;
        }
        return true;
    }

    @Override
    public double computeProb(PUAG.Node n1, PUAG.Node n2) {
        return probMatrix.get(n1).get(n2);
    }

    @Override
    public double computeGoalProb(PUAG.Node n) {
        return 0.7;
    }

    @Override
    public double computeAngle(PUAG.Node n1, PUAG.Node n2) {
        return angleMatrix.get(n1).get(n2);
    }

    @Override
    public Vec2D computeKickVec(PUAG.Node n1, PUAG.Node n2) {
        return kickVecMatrix.get(n1).get(n2);
    }

    @Override
    public Vec2D computePasspoint(PUAG.Node n1, PUAG.Node n2) {
        return passpointMatrix.get(n1).get(n2);
    }

    @Override
    public Vec2D computeRecepPoint(PUAG.Node n1, PUAG.Node n2) {
        return recepPointMatrix.get(n1).get(n2);
    }

    @Override
    public Vec2D computeGoalCenter(PUAG.Node n) {
        return null;
    }
}
