package Triton.Detection;

import java.util.ArrayList;
import Triton.Shape.Vec2D;
import Proto.MessagesRobocupSslDetection.SSL_DetectionRobot;

public class Robot {

    public class SortedDetection implements Comparable<SortedDetection> {
        public SSL_DetectionRobot detection;
        public double time; 

        public SortedDetection(SSL_DetectionRobot detection, double time) {
            this.detection = detection;
            this.time = time;
        }

        @Override
        public int compareTo(SortedDetection other) {
            if(this.time == other.time) {
                return 0;
            } else if(this.time < other.time) { // this older -> (1) greater -> lower in min-heap
                return 1;
            } else return -1;
        }
        
        @Override
        public String toString() {
            return "[" +this.time+"," + this.detection +"]";
        }

        public Vec2D getPos() {
            return new Vec2D(detection.getX(), detection.getY());
        }
    }

    private ArrayList<SortedDetection> detections = new ArrayList<SortedDetection>();
    private Vec2D vel;
    private double angVel;
    private Team team;
    private int ID;

    public Robot(Team team, int ID) {
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
        // return when there is no previous data
        if (detections.size() == 1) {
            vel = new Vec2D(0, 0);
            angVel = 0.0;
            return;
        }
        SortedDetection secondLatest = detections.get(detections.size() - 2);
        double dt = (latest.time - secondLatest.time) * 1000; 
        if (dt > 0) {
            vel = latest.getPos().subtract(secondLatest.getPos()).multiply(1 / dt);
            angVel = (latest.detection.getOrientation() - secondLatest.detection.getOrientation()) / dt;
        }
    }

    public Vec2D getPos() {
        return detections.get(detections.size() - 1).getPos();
    }

    public double getOrient() {
        return detections.get(detections.size() - 1).detection.getOrientation();
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