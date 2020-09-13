package Triton.WorldSim;

import Triton.Detection.Team;
import Triton.Shape.Vec2D;

public class RobotSim extends PhysicsObject {
    private Team team;
    private int ID;
    private double angle, angleVel;

    public RobotSim(Team team, int ID, Vec2D pos, Vec2D vel, double angle, double angleVel) {
        super(pos, vel);
        this.team = team;
        this.ID = ID;
        this.angle = angle;
        this.angleVel = angleVel;
    }

    public RobotSim(Vec2D pos) {
        super(pos, new Vec2D(0, 0));
    }

    public void update(double delta) {
        super.update(delta);
        angle += angleVel * delta;
    }

    public Team getTeam() {
        return team;
    }

    public int getID() {
        return ID;
    }

    public double getOrient() {
        return angle;
    }

    public void setOrient(double angle) {
        this.angle = angle;
    }

    public double getAngleVel() {
        return angleVel;
    }

    public void setAngleVel(double angleVel) {
        this.angleVel = angleVel;
    }
}
