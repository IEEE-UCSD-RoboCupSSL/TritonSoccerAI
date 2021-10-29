package triton.coreModules.ai.dijkstra.computables;

import triton.coreModules.ai.dijkstra.exceptions.NonExistentNodeException;
import triton.coreModules.ai.dijkstra.Pdg;
import triton.misc.math.linearAlgebra.Vec2D;

public interface KickVecCompute {
    Vec2D computeKickVec(Pdg.Node node1, Pdg.Node node2) throws NonExistentNodeException;

    Vec2D computeGoalKickVec(Pdg.Node node);
}
