package Triton.CoreModules.AI.TritonProbDijkstra.Exceptions;

import Triton.CoreModules.AI.TritonProbDijkstra.PDG;

public class NoSuchEdgeException extends GraphIOException{
    public NoSuchEdgeException(PDG.Node n1, PDG.Node n2) {
        super("Trying to access an non-existent edge between " + n1 + " and " + n2);
    }
}
