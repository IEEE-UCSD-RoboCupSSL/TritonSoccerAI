package Triton.CoreModules.AI.TritonProbDijkstra;

import Triton.Config.GlobalVariblesAndConstants.GvcGeometry;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class PUAG { //Probability Undirected Acyclic Graph

    private final Node startNode;
    private final Node endNode;
    private final ArrayList<Node> nodeList = new ArrayList<>();

    private final HashMap<Node, HashMap<Node, Edge>> adjList = new HashMap<>();

    public PUAG(Node startNode, Node endNode, List<Node> middleNodes) {
        // construct graph
        this.startNode = startNode;
        this.endNode = endNode;

        nodeList.add(startNode);
        nodeList.addAll(middleNodes);
        nodeList.add(endNode);

        for (Node node1 : nodeList) {
            for (Node node2 : nodeList) {
                if (node1 == node2) {
                    continue;
                }
                Edge sharedEdge = new Edge();

                if (!adjList.containsKey(node1)) {
                    adjList.put(node1, new HashMap<>());
                } else {
                    adjList.get(node1).put(node2, sharedEdge);
                }

                if (!adjList.containsKey(node2)) {
                    adjList.put(node2, new HashMap<>());
                } else {
                    adjList.get(node2).put(node1, sharedEdge);
                }
            }
        }
    }

    public Set<Node> getNodeSet() {
        return new HashSet<>(nodeList);
    }

    public int getNumNodes() {
        assert nodeList.size() == adjList.size();
        return nodeList.size();
    }

    public List<Node> getAdjacentNodes(Node node) {
        HashMap<Node, Edge> neighbors = adjList.get(node);
        if (neighbors == null) {
            return null;
        }

        return new ArrayList<>(neighbors.keySet());
    }

    public Edge getEdge(Node node1, Node node2) {
        return adjList.get(node1).get(node2);
    }

    public boolean setEdgeProb(Node node1, Node node2, double prob) {
        try {
            adjList.get(node1).get(node2).setProb(prob);
        } catch (NullPointerException e) {
            return false;
        }

        return true;
    }

    public abstract static class Node {
    }

    @Getter
    @Setter
    public static class AllyNode extends Node {
        private final Ally bot;

        public AllyNode(Ally bot) {
            this.bot = bot;
        }
    }

    @Getter
    @Setter
    public static class AllyRecepNode extends AllyNode {
        private Vec2D receptionPoint;
        private double angle;

        public AllyRecepNode(Ally bot) {
            super(bot);
        }
    }

    @Getter
    @Setter
    public static class AllyPassNode extends AllyNode {
        private Vec2D passPoint;
        private double angle;
        private Vec2D kickVec;

        public AllyPassNode(Ally bot) {
            super(bot);
        }
    }

    @Getter
    @Setter
    public static class GoalNode extends Node {
        private Vec2D goalCenter = GvcGeometry.GOAL_CENTER_FOE;
    }

    @Getter
    @Setter
    public static class Edge {
        private Vec2D passPoint;
        private double angle;
        private Vec2D kickVec;
        private double prob;
    }

}
