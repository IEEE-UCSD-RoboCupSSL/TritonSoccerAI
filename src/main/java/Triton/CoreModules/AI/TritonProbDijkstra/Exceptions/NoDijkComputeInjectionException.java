package Triton.CoreModules.AI.TritonProbDijkstra.Exceptions;

public class NoDijkComputeInjectionException extends RuntimeException{
    public NoDijkComputeInjectionException() {
        super("No DijkCompute object was ever injected. Cannot compute!");
    }
}
