package Triton.CoreModules.AI.TritonProbDijkstra;

import Triton.Config.GlobalVariblesAndConstants.GvcGeometry;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Robot;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import org.ejml.All;

import java.util.*;

public class PUAG { //Probability Undirected Acyclic Graph

    private Node startNode;
    private Node endNode;

    public PUAG(Node startNode, Node endNode, List<Node> middleNodes) {
        // construct graph
        this.startNode = startNode;
        this.endNode = endNode;

        startNode.addNeighbor(endNode);
        for(Node node : middleNodes) {
            startNode.addNeighbor(node);
        }

        for(Node node : middleNodes) {
            node.addNeighbor(endNode);
            for(Node nextNode : middleNodes) {
                if(!nextNode.equals(node)) {
                    node.addNeighbor(nextNode);
                }
            }
        }
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


    public abstract static class Node {
        private List<Node> adjNodes = new LinkedList<>();
        public List<Node> getAdjacentNodes() {
            return adjNodes;
        }
        public void addNeighbor(Node neighborNode) {
            adjNodes.add(neighborNode);
        }
    }

    public static class AllyNode extends Node {
        public Ally bot;
        public AllyNode(Ally bot) {
            this.bot = bot;
        }
    }

    public static class AllyHolderNode extends AllyNode {
        public AllyHolderNode(Ally bot) {
            super(bot);
        }
    }

    public static class GoalNode extends Node {
        Vec2D goalCenter = GvcGeometry.GOAL_CENTER_FOE;
    }

}
