package Triton.CoreModules.AI.TritonProbDijkstra.Computables;

import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.NonExistentNodeException;
import Triton.CoreModules.AI.TritonProbDijkstra.PDG;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

public interface KickVecCompute {
    Vec2D computeKickVec(PDG.Node node1, PDG.Node node2) throws NonExistentNodeException;

    Vec2D computeGoalKickVec(PDG.Node node);
}
