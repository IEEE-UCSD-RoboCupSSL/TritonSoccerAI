package Triton.CoreModules.AI.TritonProbDijkstra.Computables;

import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

public interface KickVecCompute {
    Vec2D computeKickVec(PUAG.Node node1, PUAG.Node node2);
}
