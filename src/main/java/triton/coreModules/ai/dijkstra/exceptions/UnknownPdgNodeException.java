package triton.coreModules.ai.dijkstra.exceptions;

import triton.coreModules.ai.dijkstra.Pdg;

public class UnknownPdgNodeException extends GraphIOException{
    public UnknownPdgNodeException(Pdg.Node n){
        super("Received unknown PDG node of type: " + n.getClass().getName());
    }
}
