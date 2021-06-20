package Triton.CoreModules.AI.TritonProbDijkstra.Exceptions;

public class InvalidDijkstraGraphException extends Exception{
    public InvalidDijkstraGraphException(){
        super("Invalid graph injected into `TritonDijkstra`.");
    }
}
