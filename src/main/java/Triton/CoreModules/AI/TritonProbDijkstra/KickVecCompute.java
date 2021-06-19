package Triton.CoreModules.AI.TritonProbDijkstra;

import Triton.Misc.Math.LinearAlgebra.Vec2D;

public interface KickVecCompute {
    Vec2D computeKickVec(PUAG.Node node1, PUAG.Node node2);
}
