package Triton.Detection;

import java.util.PriorityQueue;
import Triton.Geometry.Point2D;
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

        public Point2D getPos() {
            return new Point2D(detection.getX(), detection.getY());
        }
    }

    private PriorityQueue<SortedDetection> detections = new PriorityQueue<SortedDetection>();
    private Point2D vel;
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
        SortedDetection secondLatest = detections.peek();
        SortedDetection latest = new SortedDetection(detection, time);
        detections.add(latest);
        double dt = (latest.time - secondLatest.time) * 1000;
        vel = latest.getPos().subtract(secondLatest.getPos()).multiply(1 / dt);
        angVel = (latest.detection.getOrientation() - secondLatest.detection.getOrientation()) / dt;
    }

    public Point2D getPos() {
        return detections.peek().getPos();
    }

    public double getOrient() {
        return detections.peek().detection.getOrientation();
    }
    public Point2D getVel() { 
        return vel;
    }

    public double getAngularVelocity() {
        return angVel;
    }

    public double getHeight() {
        return detections.peek().detection.getHeight();
    }
    
    public void commandPosition(Point2D position) {
    }

    public void commandVelocity(Point2D vel) {
    }
}