package Triton.CoreModules.AI.TritonProbDijkstra.Exceptions;

import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;

public class NonExistentNodeException extends RuntimeException{
    public NonExistentNodeException(PUAG.Node node) {
        super("Cannot find node in node list. Node: " + node.toString() + " with Bot Id: " + node.getNodeBotIdString());
    }
}
