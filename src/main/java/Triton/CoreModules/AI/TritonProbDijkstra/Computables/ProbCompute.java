package Triton.CoreModules.AI.TritonProbDijkstra.Computables;

import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.NonExistentNodeException;
import Triton.CoreModules.AI.TritonProbDijkstra.PDG;

public interface ProbCompute {
    double computeProb(PDG.Node n1, PDG.Node n2) throws NonExistentNodeException;

    double computeGoalProb(PDG.Node n);
}
