package Triton.CoreModules.AI.TritonProbDijkstra.Computables;

import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

public interface RecepPointCompute {
    Vec2D computeRecepPoint(PUAG.Node n1, PUAG.Node n2);
}
