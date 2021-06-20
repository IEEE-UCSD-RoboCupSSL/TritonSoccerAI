package Triton.CoreModules.AI.TritonProbDijkstra.Computables;

import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

public interface PasspointCompute {
    Vec2D computePasspoint(PUAG.Node node1, PUAG.Node node2);
}
