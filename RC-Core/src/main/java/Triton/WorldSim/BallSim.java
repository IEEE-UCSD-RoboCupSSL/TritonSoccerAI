package Triton.WorldSim;

import Triton.Shape.Vec2D;

public class BallSim extends PhysicsObject {

    public BallSim(Vec2D pos, Vec2D vel) {
        super(pos, vel);
    }

    public BallSim(Vec2D pos) {
        super(pos, new Vec2D(0, 0));
    }
}