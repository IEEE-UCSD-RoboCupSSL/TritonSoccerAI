package Triton.CoreModules.AI.TritonProbDijkstra.Exceptions;

import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;

import java.util.HashMap;

public class InvalidNodeIndexException extends RuntimeException{
    public InvalidNodeIndexException(HashMap<PUAG.Node, Integer> nodeToIndexMap, PUAG.Node node) {
        super("nodeToIndexMap returned an erroneous index: [" + nodeToIndexMap.toString() + "] with returned node: " + node.toString());
    }
}
