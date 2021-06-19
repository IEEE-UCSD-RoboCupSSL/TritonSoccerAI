package Triton.CoreModules.AI.TritonProbDijkstra;

import Triton.Config.GlobalVariblesAndConstants.GvcGeometry;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Robot;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.ejml.All;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PUAG { //Probability Undirected Acyclic Graph

    private Node startNode;
    private Node endNode;
    private Set<Node> nodeSet;

    public PUAG(Node startNode, Node endNode, List<Node> middleNodes) {
        // construct graph
        this.startNode = startNode;
        this.endNode = endNode;

        startNode.addNeighbor(endNode);
        for (Node node : middleNodes) {
            startNode.addNeighbor(node);
        }

        for (Node node : middleNodes) {
            node.addNeighbor(endNode);
            for (Node nextNode : middleNodes) {
                if (!nextNode.equals(node)) {
                    node.addNeighbor(nextNode);
                }
            }
        }

        nodeSet.add(startNode);
        nodeSet.add(endNode);
        nodeSet.addAll(middleNodes);
    }

    public Node getStartNode() {
        return startNode;
    }

    public void setStartNode(Node startNode) {
        this.startNode = startNode;
    }

    public Node getEndNode() {
        return endNode;
    }

    public void setEndNode(Node endNode) {
        this.endNode = endNode;
    }

    public Set<Node> getNodeSet(){
        return nodeSet;
    }
    public int getNumNodes(){
        return nodeSet.size();
    }

    public abstract static class Node {

        private final List<Node> adjNodes = new ArrayList<>();



        public List<Node> getAdjacentNodes() {
            return adjNodes;
        }

        public void addNeighbor(Node neighborNode) {
            adjNodes.add(neighborNode);
        }
    }


    @Getter
    @Setter
    public static class AllyNode extends Node {
        private final Ally bot;
        private Vec2D receptionPoint;
        private double angle;
        private Vec2D kickVec;

        public AllyNode(Ally bot) {
            this.bot = bot;
        }
    }

    @Getter
    @Setter
    public static class AllyHolderNode extends Node {
        private final Ally bot;
        private Vec2D passPoint;
        private double angle;
        private Vec2D kickVec;

        public AllyHolderNode(Ally bot) {
            this.bot = bot;
        }
    }

    @Getter
    @Setter
    public static class GoalNode extends Node {
        private Vec2D goalCenter = GvcGeometry.GOAL_CENTER_FOE;
    }

}
