package triton.manualTests;

import triton.config.Config;
import triton.coreModules.ai.dijkstra.exceptions.*;

public interface TritonTestable {
    boolean test(Config config) throws GraphIOException, NoDijkComputeInjectionException;
}
