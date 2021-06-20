package Triton.CoreModules.AI.TritonProbDijkstra.Computables;

import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

public interface GoalCenterCompute {
    Vec2D computeGoalCenter(PUAG.Node n);
}
