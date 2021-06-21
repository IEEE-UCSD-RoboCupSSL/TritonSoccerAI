package Triton.CoreModules.AI.TritonProbDijkstra.Computables;

import Triton.CoreModules.AI.TritonProbDijkstra.PDG;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

public interface PassPointCompute {
    Vec2D computePasspoint(PDG.Node node1, PDG.Node node2);
}
