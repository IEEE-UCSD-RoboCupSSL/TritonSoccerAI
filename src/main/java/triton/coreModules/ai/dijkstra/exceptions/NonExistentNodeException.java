package triton.coreModules.ai.dijkstra.exceptions;

import triton.coreModules.ai.dijkstra.Pdg;

public class NonExistentNodeException extends GraphIOException{
    public NonExistentNodeException(Pdg.Node node) {
        super("Cannot find node in node list. Node: " + node.toString() + " with Bot Id: " + node.getNodeBotIdString());
    }
}
