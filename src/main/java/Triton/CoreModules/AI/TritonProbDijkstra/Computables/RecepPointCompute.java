package Triton.CoreModules.AI.TritonProbDijkstra.Computables;

import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.NonExistentNodeException;
import Triton.CoreModules.AI.TritonProbDijkstra.PDG;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

public interface RecepPointCompute {
    Vec2D computeRecepPoint(PDG.Node n1, PDG.Node n2) throws NonExistentNodeException;
}
