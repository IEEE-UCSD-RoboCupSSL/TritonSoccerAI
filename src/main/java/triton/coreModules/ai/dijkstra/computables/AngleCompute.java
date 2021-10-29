package triton.coreModules.ai.dijkstra.computables;

import triton.coreModules.ai.dijkstra.Pdg;

public interface AngleCompute {
    double computeAngle(Pdg.Node n1, Pdg.Node n2);

    double computeGoalAngle(Pdg.Node n);
}
