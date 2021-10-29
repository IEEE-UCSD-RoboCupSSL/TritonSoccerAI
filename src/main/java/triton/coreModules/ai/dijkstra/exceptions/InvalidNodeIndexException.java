package triton.coreModules.ai.dijkstra.exceptions;

import triton.coreModules.ai.dijkstra.Pdg;

import java.util.HashMap;

public class InvalidNodeIndexException extends GraphIOException{
    public InvalidNodeIndexException(HashMap<Pdg.Node, Integer> nodeToIndexMap, Pdg.Node node) {
        super("nodeToIndexMap returned an erroneous index: [" + nodeToIndexMap.toString() + "] with returned node: " + node.toString());
    }
}
