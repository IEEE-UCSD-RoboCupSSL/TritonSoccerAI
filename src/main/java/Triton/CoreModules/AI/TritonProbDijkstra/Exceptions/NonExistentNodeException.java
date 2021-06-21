package Triton.CoreModules.AI.TritonProbDijkstra.Exceptions;

import Triton.CoreModules.AI.TritonProbDijkstra.PDG;

public class NonExistentNodeException extends GraphIOException{
    public NonExistentNodeException(PDG.Node node) {
        super("Cannot find node in node list. Node: " + node.toString() + " with Bot Id: " + node.getNodeBotIdString());
    }
}
