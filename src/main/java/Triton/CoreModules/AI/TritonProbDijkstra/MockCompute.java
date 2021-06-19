package Triton.CoreModules.AI.TritonProbDijkstra;

import Triton.CoreModules.Robot.Ally.Ally;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import lombok.Getter;
import lombok.Setter;

import java.security.KeyException;
import java.util.HashMap;
import java.util.Set;

@Getter
@Setter
public class MockCompute implements ProbCompute, AngleCompute, KickVecCompute, PassPointCompute{
    private HashMap<PUAG.Node, HashMap<PUAG.Node, Double>> probMatrix = new HashMap<>();
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

    @Override
    public double computeProb(PUAG.Node n1, PUAG.Node n2) {
        return probMatrix.get(n1).get(n2);
    }

    public boolean setProb(PUAG.Node n1, PUAG.Node n2, Double prob) {
        try {
            probMatrix.get(n1).put(n2, prob);
        } catch (NullPointerException e){
            return false;
        }
        return true;
    }

    @Override
    public double computeAngle(PUAG.Node n1, PUAG.Node n2) {
        return 0;
    }

    @Override
    public Vec2D computeKickVec(PUAG.Node node1, PUAG.Node node2) {
        return null;
    }

    @Override
    public Vec2D computePassPoint(PUAG.Node node1, PUAG.Node node2) {
        return null;
    }
}
