package Triton.CoreModules.AI.TritonProbDijkstra.ComputableImpl;

import Triton.CoreModules.AI.TritonProbDijkstra.Computables.DijkCompute;
import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;
import Triton.CoreModules.Robot.RobotSnapshot;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.RWLockee;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class MockCompute implements DijkCompute {
    private final HashMap<PUAG.Node, HashMap<PUAG.Node, Double>> probMatrix = new HashMap<>();
    private final HashMap<PUAG.Node, HashMap<PUAG.Node, Double>> angleMatrix = new HashMap<>();
    private final HashMap<PUAG.Node, HashMap<PUAG.Node, Vec2D>> kickVecMatrix = new HashMap<>();
    private final HashMap<PUAG.Node, HashMap<PUAG.Node, Vec2D>> passPointMatrix = new HashMap<>();
    private final HashMap<PUAG.Node, HashMap<PUAG.Node, Vec2D>> recepPointMatrix = new HashMap<>();

//    private final Table<PUAG.Node, PUAG.Node, Double> probMatrix = HashBasedTable.create();
//    private final Table<PUAG.Node, PUAG.Node, Double> angleMatrix = HashBasedTable.create();
//    private final Table<PUAG.Node, PUAG.Node, Vec2D> kickVecMatrix = HashBasedTable.create();
//    private final Table<PUAG.Node, PUAG.Node, Vec2D> passPointMatrix = HashBasedTable.create();
//    private final Table<PUAG.Node, PUAG.Node, Vec2D> recepPointMatrix = HashBasedTable.create();

    private PUAG graph;

    public MockCompute(PUAG graph) {
        Set<PUAG.Node> nodeSet = graph.getNodeSet();

        for (PUAG.Node node1 : nodeSet) {
            HashMap<PUAG.Node, Double> neighborProbMap = new HashMap<>();
            probMatrix.put(node1, neighborProbMap);

            HashMap<PUAG.Node, Double> neighborAngleMap = new HashMap<>();
            angleMatrix.put(node1, neighborAngleMap);

            HashMap<PUAG.Node, Vec2D> neighborKickVecMap = new HashMap<>();
            kickVecMatrix.put(node1, neighborKickVecMap);

            HashMap<PUAG.Node, Vec2D> neighborPassPointMap = new HashMap<>();
            passPointMatrix.put(node1, neighborPassPointMap);

            HashMap<PUAG.Node, Vec2D> neighborRecepPointMap = new HashMap<>();
            recepPointMatrix.put(node1, neighborRecepPointMap);

            for (PUAG.Node node2 : graph.getAdjacentNodes(node1)) {

                if (node1 == node2) {
                    continue;
                }

                Double sharedProbDouble = 0.0;
                Double sharedAngleDouble = 0.0;
                Vec2D sharedKickVec = new Vec2D(0, 0);
                Vec2D sharedPassPointVec = new Vec2D(0, 0);
                Vec2D sharedRecepPointVec = new Vec2D(0, 0);

                if (!probMatrix.containsKey(node1)) {
                    probMatrix.put(node1, new HashMap<>());
                    angleMatrix.put(node1, new HashMap<>());
                    kickVecMatrix.put(node1, new HashMap<>());
                    passPointMatrix.put(node1, new HashMap<>());
                    recepPointMatrix.put(node1, new HashMap<>());
                } else {
                    probMatrix.get(node1).put(node2, sharedProbDouble);
                    angleMatrix.get(node1).put(node2, sharedAngleDouble);
                    kickVecMatrix.get(node1).put(node2, sharedKickVec);
                    passPointMatrix.get(node1).put(node2, sharedPassPointVec);
                    recepPointMatrix.get(node1).put(node2, sharedRecepPointVec);
                }

                if (!probMatrix.containsKey(node2)) {
                    probMatrix.put(node2, new HashMap<>());
                    angleMatrix.put(node2, new HashMap<>());
                    kickVecMatrix.put(node2, new HashMap<>());
                    passPointMatrix.put(node2, new HashMap<>());
                    recepPointMatrix.put(node2, new HashMap<>());
                } else {
                    probMatrix.get(node2).put(node1, sharedProbDouble);
                    angleMatrix.get(node2).put(node1, sharedAngleDouble);
                    kickVecMatrix.get(node2).put(node1, sharedKickVec);
                    passPointMatrix.get(node2).put(node1, sharedPassPointVec);
                    recepPointMatrix.get(node2).put(node1, sharedRecepPointVec);
                }
            }

        }
    }

    public boolean setProb(PUAG.Node n1, PUAG.Node n2, double prob) {
        try {
            Double sharedDouble = prob;
            probMatrix.get(n1).put(n2, sharedDouble);
            probMatrix.get(n2).put(n1, sharedDouble);
        } catch (NullPointerException e){
            return false;
        }

        assert probMatrix.get(n1).get(n2) == probMatrix.get(n2).get(n1);
        return true;
    }

    public boolean setAllProb(double prob){
        try{
            for (Map.Entry<PUAG.Node, HashMap<PUAG.Node, Double>> nodeHashMapEntry : probMatrix.entrySet()) {
                for (Map.Entry<PUAG.Node, Double> nodeDoubleEntry : nodeHashMapEntry.getValue().entrySet()) {
                    nodeDoubleEntry.setValue(prob);
                }
            }
        } catch (NullPointerException e){
            return false;
        }
        return true;
    }

    public boolean setAngle(PUAG.Node n1, PUAG.Node n2, double angle) {
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
            passPointMatrix.get(n1).put(n2, passpoint);
        } catch (NullPointerException e){
            return false;
        }

        assert passPointMatrix.get(n1).get(n2) == passPointMatrix.get(n2).get(n1);
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
        HashMap<PUAG.Node, Double> nodeDoubleHashMap = probMatrix.get(n1);
        if(nodeDoubleHashMap == null){
            System.out.println("HashMap does not contain node: " + n1.toString());
        }

        Double aDouble = nodeDoubleHashMap.get(n2);
        if (aDouble == null){
            System.out.println("HashMap does not contain node: " + n2.toString());
        }
        return aDouble;
    }

    @Override
    public double computeGoalProb(PUAG.Node n) {
        return 0.7;
    }

    @Override
    public double computeAngle(PUAG.Node n1, PUAG.Node n2) {
        HashMap<PUAG.Node, Double> nodeDoubleHashMap = angleMatrix.get(n1);
        if(nodeDoubleHashMap == null){
            System.out.println("HashMap does not contain node: " + n1.toString());
        }

        Double aDouble = nodeDoubleHashMap.get(n2);
        if (aDouble == null){
            System.out.println("HashMap does not contain node: " + n2.toString());
        }
        return aDouble;
    }

    @Override
    public Vec2D computeKickVec(PUAG.Node n1, PUAG.Node n2) {
        HashMap<PUAG.Node, Vec2D> nodeDoubleHashMap = kickVecMatrix.get(n1);
        if(nodeDoubleHashMap == null){
            System.out.println("HashMap does not contain node: " + n1.toString());
        }

        Vec2D aVec = nodeDoubleHashMap.get(n2);
        if (aVec == null){
            System.out.println("HashMap does not contain node: " + n2.toString());
        }
        return aVec;
    }

    @Override
    public Vec2D computePasspoint(PUAG.Node n1, PUAG.Node n2) {
        HashMap<PUAG.Node, Vec2D> nodeDoubleHashMap = passPointMatrix.get(n1);
        if(nodeDoubleHashMap == null){
            System.out.println("HashMap does not contain node: " + n1.toString());
        }

        Vec2D aVec = nodeDoubleHashMap.get(n2);
        if (aVec == null){
            System.out.println("HashMap does not contain node: " + n2.toString());
        }
        return aVec;
    }

    @Override
    public Vec2D computeRecepPoint(PUAG.Node n1, PUAG.Node n2) {
        HashMap<PUAG.Node, Vec2D> nodeDoubleHashMap = recepPointMatrix.get(n1);
        if(nodeDoubleHashMap == null){
            System.out.println("HashMap does not contain node: " + n1.toString());
        }

        Vec2D aVec = nodeDoubleHashMap.get(n2);
        if (aVec == null){
            System.out.println("HashMap does not contain node: " + n2.toString());
        }
        return aVec;
    }

    @Override
    public Vec2D computeGoalCenter(PUAG.Node n) {
        return null;
    }

    @Override
    public void setSnapShots(ArrayList<RobotSnapshot> allySnaps, ArrayList<RobotSnapshot> foeSnaps, RWLockee<Vec2D> ballSnap) {

    }
}
