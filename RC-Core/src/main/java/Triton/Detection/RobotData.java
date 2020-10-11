package Triton.Detection;

import java.util.ArrayList;
import java.util.Collections;

import Triton.Shape.Vec2D;
import Proto.MessagesRobocupSslDetection.SSL_DetectionRobot;

public class RobotData {

    public static final int MAX_SIZE = 10;

    public class SortedDetection implements Comparable<SortedDetection> {
        public SSL_DetectionRobot detection;
        public double time; 

        public SortedDetection(SSL_DetectionRobot detection, double time) {
            this.detection = detection;
            this.time = time;
        }

        @Override
        public int compareTo(SortedDetection other) {
            return (int) (other.time - time);
        }
        
        @Override
        public String toString() {
            return "[" +this.time+"," + this.detection +"]";
        }

        public Vec2D getPos() {
            return new Vec2D(detection.getX(), detection.getY());
        }

        public double getAngle() {
            return detection.getOrientation();
        }
    }

    private ArrayList<SortedDetection> detections = new ArrayList<SortedDetection>();
    private Vec2D pos;
    private Vec2D vel;
    private double angle;
    private double angVel;
    private Team team;
    private int ID;

    public RobotData(Team team, int ID) {
        this.team = team;
        this.ID = ID;
    }

    public Team getTeam() {
        return this.team;
    }

    public int getID() {
        return this.ID;
    }

    public void update(SSL_DetectionRobot detection, double time) {
        SortedDetection latest = new SortedDetection(detection, time);
        detections.add(latest);
        Collections.sort(detections);

        SortedDetection newest = detections.get(0);
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
            detections.remove(0);
        }

        SortedDetection secondLatest = detections.get(detections.size() - 2);
        double dt = (latest.time - secondLatest.time) * 1000; 
        if (dt > 0) {
            vel = latest.getPos().sub(secondLatest.getPos()).mult(1 / dt);
            angVel = (latest.detection.getOrientation() - secondLatest.detection.getOrientation()) / dt;
        }
    }

    public Vec2D getPos() {
        return pos;
    }

    public double getOrient() {
        return angle;
    }
    public Vec2D getVel() { 
        return vel;
    }

    public double getAngularVelocity() {
        return angVel;
    }

    public double getHeight() {
        return detections.get(detections.size() - 1).detection.getHeight();
    }
    
    public void commandPosition(Vec2D position) {
    }

    public void commandVelocity(Vec2D vel) {
    }
}