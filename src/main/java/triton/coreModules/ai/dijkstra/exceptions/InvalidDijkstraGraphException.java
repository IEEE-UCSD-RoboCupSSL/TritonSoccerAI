package triton.coreModules.ai.dijkstra.exceptions;

public class InvalidDijkstraGraphException extends GraphIOException{
    public InvalidDijkstraGraphException(){
        super("Invalid graph injected into `TritonDijkstra`.");
    }
}
