package Triton.CoreModules.AI.TritonProbDijkstra.Exceptions;

public class InvalidDijkstraGraphException extends RuntimeException{
    public InvalidDijkstraGraphException(){
        super("Invalid graph injected into `TritonDijkstra`.");
    }
}
