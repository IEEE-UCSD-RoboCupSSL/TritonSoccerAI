package Triton.CoreModules.AI.TritonProbDijkstra;

import Triton.CoreModules.Robot.Robot;

import java.util.*;

public class PUAG { //Probability Undirected Acyclic Graph


    private List<Node> nodes;
    private Node startNode;
    private Node endNode;

    public void addNode(Node nodeA) {
        nodes.add(nodeA);
    }

    public PUAG(Node startNode, Node endNode, List<Node> middleNodes) {
        // construct graph
        this.startNode = startNode;
        this.endNode = endNode;
        for(Node node : middleNodes) {
            startNode.addNeighbor(node);
        }
        // ...
    }


    public static class Node {
        private Robot bot;
        // ...

        private List<Node> adjNodes = new LinkedList<>();
        public List<Node> getAdjacentNodes() {
            return adjNodes;
        }
        public void addNeighbor(Node neighborNode) {
            adjNodes.add(neighborNode);
        }
    }

}
