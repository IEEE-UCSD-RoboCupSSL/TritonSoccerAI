package Triton.CoreModules.AI.TritonProbDijkstra.Exceptions;

import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;

public class NoSuchEdgeException extends GraphIOException{
    public NoSuchEdgeException(PUAG.Node n1, PUAG.Node n2) {
        super("Trying to access an non-existent edge between " + n1 + " and " + n2);
    }
}
