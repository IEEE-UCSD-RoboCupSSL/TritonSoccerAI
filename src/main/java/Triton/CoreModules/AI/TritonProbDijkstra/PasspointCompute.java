package Triton.CoreModules.AI.TritonProbDijkstra;

import Triton.Misc.Math.LinearAlgebra.Vec2D;

public interface PasspointCompute {
    Vec2D computePasspoint(PUAG.Node node1, PUAG.Node node2);
}
