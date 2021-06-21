package Triton.CoreModules.AI.TritonProbDijkstra.Exceptions;

import Triton.CoreModules.AI.TritonProbDijkstra.PDG;

public class UnknownPdgNodeException extends GraphIOException{
    public UnknownPdgNodeException(PDG.Node n){
        super("Received unknown PDG node of type: " + n.getClass().getName());
    }
}
