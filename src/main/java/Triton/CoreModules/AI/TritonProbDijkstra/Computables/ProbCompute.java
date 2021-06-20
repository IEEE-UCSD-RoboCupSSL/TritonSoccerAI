package Triton.CoreModules.AI.TritonProbDijkstra.Computables;

import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;

public interface ProbCompute {
    double computeProb(PUAG.Node n1, PUAG.Node n2);

    double computeGoalProb(PUAG.Node n);
}
