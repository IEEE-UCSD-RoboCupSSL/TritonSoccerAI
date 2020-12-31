package Triton.Modules.Detection;

import Proto.MessagesRobocupSslDetection.SSL_DetectionRobot;
import Triton.Dependencies.Shape.Vec2D;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Stores data about robot object
 */
public class RobotData {

    public static final int MAX_SIZE = 10;
    private final Team team;
    private final int ID;
    private final ArrayList<SortedDetectionRobot> detections;
    private Vec2D pos;
    private Vec2D vel;
    private double angle;
    private double angVel;
    public RobotData(Team team, int ID) {
        this.team = team;
        this.ID = ID;
        detections = new ArrayList<>();
        pos = new Vec2D(0, 0);
        vel = new Vec2D(0, 0);
        angle = 0;
        angVel = 0;
    }

    /**
     * Updates ArrayList of SortedDetections and calculates the current velocity of the robot
     * @param detection SSL_DetectionRobot data to use to update
     * @param time time of detection
     */
    public void update(SSL_DetectionRobot detection, double time) {
        SortedDetectionRobot latest = new SortedDetectionRobot(detection, time);
        detections.add(latest);
        Collections.sort(detections, Collections.reverseOrder());

        SortedDetectionRobot newest = detections.get(0);
        pos = newest.getPos();
        angle = newest.getAngle();

        // return when there is no previous data
        if (detections.size() == 1) {
            vel = new Vec2D(0, 0);
            angVel = 0.0;
            return;
        }
        // if there are more than MAX_SIZE data, remove the oldest
        else if (detections.size() > MAX_SIZE) {
            detections.remove(detections.size() - 1);
        }

        SortedDetectionRobot secondLatest = detections.get(detections.size() - 2);
        double dt = (latest.time - secondLatest.time) * 1000;
        if (dt > 0) {
            vel = latest.getPos().sub(secondLatest.getPos()).mult(1 / dt);
            angVel = (latest.detection.getOrientation() - secondLatest.detection.getOrientation()) / dt;
        }
    }

    /**
     * @return team robot belongs to
     */
    public Team getTeam() {
        return team;
    }

    /**
     * @return ID of robot
     */
    public int getID() {
        return ID;
    }

    /**
     * @return current position of robot
     */
    public Vec2D getPos() {
        return pos;
    }

    /**
     * @return current orientation of the robot
     */
    public double getOrient() {
        return angle;
    }

    /**
     * @return current translational velocity of the robot
     */
    public Vec2D getVel() {
        return vel;
    }

    /**
     * @return current angular velocity of the robot
     */
    public double getAngularVelocity() {
        return angVel;
    }

    /**
     * @return current height of the robot
     */
    public double getHeight() {
        return detections.get(detections.size() - 1).detection.getHeight();
    }

    public static class SortedDetectionRobot implements Comparable<SortedDetectionRobot> {
        public SSL_DetectionRobot detection;
        public double time;

        public SortedDetectionRobot(SSL_DetectionRobot detection, double time) {
            this.detection = detection;
            this.time = time;
        }

        @Override
        public int compareTo(SortedDetectionRobot other) {
            return Double.compare(time, other.time);
        }

        @Override
        public String toString() {
            return "[" + this.time + "," + this.detection + "]";
        }

        public Vec2D getPos() {
            return new Vec2D(detection.getX(), detection.getY());
        }

        public double getAngle() {
            return detection.getOrientation();
        }
    }
}