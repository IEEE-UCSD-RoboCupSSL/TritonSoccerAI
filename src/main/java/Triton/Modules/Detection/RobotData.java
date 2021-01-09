package Triton.Modules.Detection;

import Proto.MessagesRobocupSslDetection.SSL_DetectionRobot;
import Triton.Dependencies.PerspectiveConverter;
import Triton.Dependencies.Shape.Vec2D;
import Triton.Dependencies.Team;

/**
 * Stores data about robot object
 */
public class RobotData {

    public static final int MAX_SIZE = 10;
    private final Team team;
    private final int ID;

    private double time;
    private Vec2D lastPos, pos;
    private Vec2D lastVel, vel;
    private Vec2D accel;

    private double lastAngle, angle;
    private double lastAngVel, angVel;
    private double angAccel;

    public RobotData(Team team, int ID) {
        this.team = team;
        this.ID = ID;

        pos = new Vec2D(0, 0);
        vel = new Vec2D(0, 0);
        accel = new Vec2D(0, 0);
    }

    public void update(SSL_DetectionRobot detection, double time) {
        if (time < this.time)
            return;

        double timeDiff = time - this.time;

        Vec2D audienceRobotPos = new Vec2D(detection.getX(), detection.getY());
        pos = PerspectiveConverter.audienceToPlayer(audienceRobotPos);
        double audienceRobotAngle = Math.toDegrees(detection.getOrientation());
        angle = PerspectiveConverter.audienceToPlayer(audienceRobotAngle);

        if (lastPos != null)
            vel = pos.sub(lastPos).mult(1 / timeDiff);
        if (lastVel != null)
            accel = vel.sub(lastVel).mult(1 / timeDiff);

        angVel = angle - lastAngle;
        angAccel = angVel - lastAngVel;

        this.time = time;
        lastPos = pos;
        lastVel = vel;
        lastAngle = angle;
        lastAngVel = angVel;
    }

    public Team getTeam() {
        return team;
    }

    public int getID() {
        return ID;
    }

    public double getTime() {
        return time;
    }

    public Vec2D getPos() {
        return pos;
    }

    public Vec2D getVel() {
        return vel;
    }

    public Vec2D getAccel() {
        return accel;
    }

    public double getAngle() {
        return angle;
    }

    public double getAngVel() {
        return angVel;
    }

    public double getAngAccel() {
        return angAccel;
    }
}