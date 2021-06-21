package Triton.CoreModules.AI.TritonProbDijkstra.Exceptions;

import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;

import java.util.List;

public class NodesNotUniqueException extends RuntimeException{
    public NodesNotUniqueException() {
        super("Received Nodes that are not unique");
    }

    public NodesNotUniqueException(PUAG.Node startNode, PUAG.Node endNode, List<PUAG.Node> middleNodes) {
        super("Received Nodes that are not unique. startNode: [" + startNode + "] endNode: [" + endNode + "] middleNodes: [" + middleNodes + "]");
    }
}
