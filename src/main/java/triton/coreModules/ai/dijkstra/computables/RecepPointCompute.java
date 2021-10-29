package triton.coreModules.ai.dijkstra.computables;

import triton.coreModules.ai.dijkstra.exceptions.NonExistentNodeException;
import triton.coreModules.ai.dijkstra.Pdg;
import triton.misc.math.linearAlgebra.Vec2D;

public interface RecepPointCompute {
    Vec2D computeRecepPoint(Pdg.Node n1, Pdg.Node n2) throws NonExistentNodeException;

}
