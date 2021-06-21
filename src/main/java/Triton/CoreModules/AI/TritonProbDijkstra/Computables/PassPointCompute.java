package Triton.CoreModules.AI.TritonProbDijkstra.Computables;

import Triton.CoreModules.AI.TritonProbDijkstra.PDG;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

public interface PassPointCompute {
    Vec2D computePassPoint(PDG.Node node1, PDG.Node node2);

    Vec2D computePassPoint(PDG.Node node);

    Vec2D computeGoalPassPoint(PDG.Node node);
}
