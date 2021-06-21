package Triton.ManualTests;

import Triton.Config.Config;
import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.*;

public interface TritonTestable {
    boolean test(Config config) throws GraphIOException, NoDijkComputeInjectionException;
}
