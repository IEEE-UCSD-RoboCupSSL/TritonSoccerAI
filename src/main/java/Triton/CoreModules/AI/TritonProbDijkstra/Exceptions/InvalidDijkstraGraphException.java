package Triton.CoreModules.AI.TritonProbDijkstra.Exceptions;

public class InvalidDijkstraGraphException extends GraphIOException{
    public InvalidDijkstraGraphException(){
        super("Invalid graph injected into `TritonDijkstra`.");
    }
}
