package Triton.CoreModules.AI.TritonProbDijkstra;

import Triton.Config.GlobalVariblesAndConstants.GvcGeometry;
import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.NoSuchEdgeException;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.RobotSnapshot;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.RWLockee;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter
public class PUAG { //Probability Undirected Acyclic Graph

    private final Node startNode;
    private final Node endNode;
    private final HashMap<Node, Integer> nodeToIndexMap = new HashMap<>();
    private final HashMap<Node, Set<Node>> nodeNeighborSetMap = new HashMap<>();
    private final Edge[][] adjMatrix;

    public PUAG(Node startNode, Node endNode, List<Node> middleNodes) {
        // construct graph
        this.startNode = startNode;
        this.endNode = endNode;
        int index = 0;

        nodeToIndexMap.put(startNode, index++);

        for (Node middleNode : middleNodes) {
            nodeToIndexMap.put(middleNode, index++);
        }

        nodeToIndexMap.put(endNode, index);

        adjMatrix = new Edge[nodeToIndexMap.size()][nodeToIndexMap.size()];

        for (Node node1 : nodeToIndexMap.keySet()) {
            HashSet<Node> neighborSet = new HashSet<>();
            nodeNeighborSetMap.put(node1, neighborSet);
            neighborSet.addAll(nodeToIndexMap.keySet());
        }

        for (int i = 0; i < nodeToIndexMap.size(); i++) {
            for (int j = 0; j < nodeToIndexMap.size(); j++) {
                adjMatrix[i][j] = new Edge();
            }
        }
    }

    public int getIndexOfNode(Node node){
        Integer integer = nodeToIndexMap.get(node);
        if(integer == null){
            return -1;
        }
        return integer;
    }

    public Set<Node> getNodeSet() {
        return nodeToIndexMap.keySet();
    }

    public int getNumNodes() {
        return nodeToIndexMap.size();
    }

    public List<Node> getAdjacentNodes(Node node) {
        if(nodeNeighborSetMap.containsKey(node)) {
            return new ArrayList<>(nodeNeighborSetMap.get(node));
        } else {
            return null;
        }
    }

    public Edge getEdge(Node node1, Node node2) {
        if (node1.equals(node2)){
            throw new NoSuchEdgeException(node1, node2);
        }

        boolean isNode2NeighborOfNode1 = getAdjacentNodes(node1).contains(node2);
        boolean isNode1NeighborOfNode2 = getAdjacentNodes(node2).contains(node1);

        assert isNode1NeighborOfNode2 == isNode2NeighborOfNode1;

        if(isNode1NeighborOfNode2) {
            return adjMatrix[getIndexOfNode(node1)][getIndexOfNode(node2)];
        } else {
            throw new NoSuchEdgeException(node1, node2);
        }
    }

    public void setEdgeProb(Node node1, Node node2, double prob) {
        getEdge(node1, node2).setProb(prob);
    }

    @Getter
    public abstract static class Node {
        @Nullable private final Ally bot;

        public Node(@Nullable Ally bot) {
            this.bot = bot;
        }

        public String getNodeBotIdString(){
            if (bot == null){
                return "Goal";
            }

            return Integer.toString(bot.getID());
        }
    }

    public static class AllyNode extends Node {

        public AllyNode(Ally bot) {
            super(bot);
        }
    }

    @Getter
    @Setter
    @ToString
    public static class AllyRecepNode extends AllyNode {
        private Vec2D receptionPoint;
        private double angle;

        public AllyRecepNode(Ally bot) {
            super(bot);
        }
    }

    @Getter
    @Setter
    @ToString
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
    @ToString
    public static class GoalNode extends Node {
        private Vec2D goalCenter = GvcGeometry.GOAL_CENTER_FOE;

        public GoalNode() {
            super(null);
        }
    }

    @Getter
    @Setter
    @ToString
    public static class Edge {
        private Vec2D passPoint;
        private double angle;
        private Vec2D kickVec;
        private double prob;
    }

}
