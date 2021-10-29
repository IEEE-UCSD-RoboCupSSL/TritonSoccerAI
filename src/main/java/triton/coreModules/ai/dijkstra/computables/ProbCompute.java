package triton.coreModules.ai.dijkstra.computables;

import triton.coreModules.ai.dijkstra.exceptions.NonExistentNodeException;
import triton.coreModules.ai.dijkstra.Pdg;

public interface ProbCompute {
    double computeProb(Pdg.Node n1, Pdg.Node n2) throws NonExistentNodeException;

    double computeGoalProb(Pdg.Node n);
}
