package Triton.CoreModules.AI.TritonProbDijkstra.Computables;

import Triton.CoreModules.Robot.RobotSnapshot;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.RWLockee;

import java.util.ArrayList;

public interface InjectSnaps {
    void setSnapShots(ArrayList<RobotSnapshot> allySnaps, ArrayList<RobotSnapshot> foeSnaps, RWLockee<Vec2D> ballSnap);
}
