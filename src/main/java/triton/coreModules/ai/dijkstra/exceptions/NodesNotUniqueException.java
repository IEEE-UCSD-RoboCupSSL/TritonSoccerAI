package triton.coreModules.ai.dijkstra.exceptions;

import triton.coreModules.ai.dijkstra.Pdg;

import java.util.List;

public class NodesNotUniqueException extends GraphIOException{
    public NodesNotUniqueException() {
        super("Received Nodes that are not unique");
    }

    public NodesNotUniqueException(Pdg.Node startNode, Pdg.Node endNode, List<Pdg.Node> middleNodes) {
        super("Received Nodes that are not unique. startNode: [" + startNode + "] endNode: [" + endNode + "] middleNodes: [" + middleNodes + "]");
    }
}
