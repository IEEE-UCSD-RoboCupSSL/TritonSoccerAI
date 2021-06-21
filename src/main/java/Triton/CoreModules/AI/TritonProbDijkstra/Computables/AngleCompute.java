package Triton.CoreModules.AI.TritonProbDijkstra.Computables;

import Triton.CoreModules.AI.TritonProbDijkstra.PDG;

public interface AngleCompute {
    double computeAngle(PDG.Node n1, PDG.Node n2);

    double computeGoalAngle(PDG.Node n);
}
