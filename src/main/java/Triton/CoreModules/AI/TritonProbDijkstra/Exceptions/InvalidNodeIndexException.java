package Triton.CoreModules.AI.TritonProbDijkstra.Exceptions;

import Triton.CoreModules.AI.TritonProbDijkstra.PDG;

import java.util.HashMap;

public class InvalidNodeIndexException extends GraphIOException{
    public InvalidNodeIndexException(HashMap<PDG.Node, Integer> nodeToIndexMap, PDG.Node node) {
        super("nodeToIndexMap returned an erroneous index: [" + nodeToIndexMap.toString() + "] with returned node: " + node.toString());
    }
}
