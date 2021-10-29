package triton.coreModules.ai.dijkstra.computables;

import triton.coreModules.ai.dijkstra.Pdg;
import triton.misc.math.linearAlgebra.Vec2D;

public interface PassPointCompute {
    Vec2D computePassPoint(Pdg.Node node1, Pdg.Node node2);

    Vec2D computePassPoint(Pdg.Node node);

    Vec2D computeGoalPassPoint(Pdg.Node node);
}
