package Triton.CoreModules.AI.TritonProbDijkstra.Exceptions;

import Triton.CoreModules.AI.TritonProbDijkstra.PDG;

import java.util.List;

public class NodesNotUniqueException extends GraphIOException{
    public NodesNotUniqueException() {
        super("Received Nodes that are not unique");
    }

    public NodesNotUniqueException(PDG.Node startNode, PDG.Node endNode, List<PDG.Node> middleNodes) {
        super("Received Nodes that are not unique. startNode: [" + startNode + "] endNode: [" + endNode + "] middleNodes: [" + middleNodes + "]");
    }
}
