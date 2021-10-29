package triton.coreModules.ai.dijkstra.exceptions;

public class NoDijkComputeInjectionException extends InjectionException{
    public NoDijkComputeInjectionException() {
        super("No DijkCompute object was ever injected. Cannot compute!");
    }
}
