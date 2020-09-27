package Triton.WorldSim;

import Triton.Shape.Vec2D;

public class PhysicsObject {
    private Vec2D pos;
    private Vec2D vel;

    public PhysicsObject(Vec2D pos, Vec2D vel) {
        this.pos = pos;
        this.vel = vel;
    }

    public PhysicsObject(Vec2D pos) {
        this.pos = pos;
        vel = new Vec2D(0, 0);
    }

    public void update(double delta) {
        pos = pos.add(vel.mult(delta));
    }

    public Vec2D getPos() {
        return pos;
    }

    public void setPos(Vec2D pos) {
        this.pos = pos;
    }

    public Vec2D getVel() {
        return vel;
    }

    public void setVel(Vec2D vel) {
        this.vel = vel;
    }
}

