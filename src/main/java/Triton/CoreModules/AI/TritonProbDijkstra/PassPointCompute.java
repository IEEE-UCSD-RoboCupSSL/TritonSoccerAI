package Triton.CoreModules.AI.TritonProbDijkstra;

import Triton.Misc.Math.LinearAlgebra.Vec2D;

public interface PassPointCompute {
    Vec2D computePassPoint(PUAG.Node node1, PUAG.Node node2);
}
