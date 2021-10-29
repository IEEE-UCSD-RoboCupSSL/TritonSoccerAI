package triton.coreModules.ai.dijkstra.computables;

import triton.coreModules.robot.RobotSnapshot;
import triton.misc.math.linearAlgebra.Vec2D;
import triton.misc.RWLockee;

import java.util.ArrayList;

public interface InjectSnaps {
    void setSnapShots(ArrayList<RobotSnapshot> allySnaps, ArrayList<RobotSnapshot> foeSnaps, RWLockee<Vec2D> ballSnap);
}
