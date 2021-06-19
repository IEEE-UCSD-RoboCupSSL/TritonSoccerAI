package Triton.CoreModules.AI.TritonProbDijkstra;

import Triton.CoreModules.Robot.Robot;
import lombok.*;

import java.security.InvalidKeyException;
import java.security.KeyException;
import java.util.*;

@Getter
@Setter
public class ProbUag { //Probabilistic Undirected Acyclic Graph

    private Map<Node, HashMap<Node, Double>> adjList;
    private final Node startNode;
    private final Node endNode;
    private final List<Node> nodes;


    /**
     *
     * @param startNode The start node of the path
     * @param endNode The end node of the path
     * @param nodes All participating nodes
     */
    public ProbUag(Node startNode, Node endNode, List<Node> nodes) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.nodes = nodes;

        buildUnweightedGraph();
    }

    /**
     * Build an empty adj list of the graph without any information on
     * neighbors and weights.
     * Only called at the end of the constructor.
     */
    private void buildUnweightedGraph(){
        HashMap<Node, HashMap<Node, Double>> adjList = new HashMap<>();

        for (Node node : nodes) {
            adjList.put(node, new HashMap<>());
        }

        this.adjList = adjList;
    }

    public boolean setWeight(Node node1, Node node2, double weight){

        adjList.get(node1).put(node2, weight);

    }

    @Data
    public static class Node {
        private final Robot bot;

    }

}
