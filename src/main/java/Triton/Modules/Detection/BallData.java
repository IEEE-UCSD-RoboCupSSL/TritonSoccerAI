package Triton.Modules.Detection;

import Proto.MessagesRobocupSslDetection.SSL_DetectionBall;
import Triton.Dependencies.PerspectiveConverter;
import Triton.Dependencies.Shape.Vec2D;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Class to store information about the ball
 */
public class BallData {
    private double time;
    private Vec2D lastPos, pos;
    private Vec2D lastVel, vel;
    private Vec2D accel;

    public BallData() {
        pos = new Vec2D(0, 0);
        vel = new Vec2D(0, 0);
        accel = new Vec2D(0, 0);
    }

    /**
     * Updates ArrayList of SortedDetections and calculates the current velocity of the ball
     * @param detection SSL_Detection of the ball
     * @param time time of detection
     */
    public void update(SSL_DetectionBall detection, double time) {
        if (time < this.time)
            return;

        double timeDiff = time - this.time;

        Vec2D audienceBallPos = new Vec2D(detection.getX(), detection.getY());
        pos = PerspectiveConverter.audienceToPlayer(audienceBallPos);

        if (lastPos != null)
            vel = pos.sub(lastPos).mult(timeDiff);

        if (lastVel != null)
            accel = vel.sub(lastVel).mult(timeDiff);

        this.time = time;
        lastPos = pos;
        lastVel = vel;
    }

    public double getTime() {
        return time;
    }

    public Vec2D getLastPos() {
        return lastPos;
    }

    public Vec2D getPos() {
        return pos;
    }

    public Vec2D getLastVel() {
        return lastVel;
    }

    public Vec2D getVel() {
        return vel;
    }

    public Vec2D getAccel() {
        return accel;
    }
}