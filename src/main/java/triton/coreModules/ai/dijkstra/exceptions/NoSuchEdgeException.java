package triton.coreModules.ai.dijkstra.exceptions;

import triton.coreModules.ai.dijkstra.Pdg;

public class NoSuchEdgeException extends GraphIOException{
    public NoSuchEdgeException(Pdg.Node n1, Pdg.Node n2) {
        super("Trying to access an non-existent edge between " + n1 + " and " + n2);
    }
}
