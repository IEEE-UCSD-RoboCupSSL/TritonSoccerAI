package Triton.Detection;

import java.util.ArrayList;
import Triton.Geometry.Point2D;
import Proto.MessagesRobocupSslDetection.SSL_DetectionBall;

public class Ball {

    public class SortedDetection implements Comparable<SortedDetection> {

        public SSL_DetectionBall detection;
        public double time;

        public SortedDetection(SSL_DetectionBall detection, double time) {
            this.detection = detection;
            this.time = time;
        }

        @Override
        public int compareTo(SortedDetection other) {
            if (this.time == other.time) {
                return 0;
            } else if (this.time < other.time) { // this older -> (1) greater -> lower in min-heap
                return 1;
            } else
                return -1;
        }

        @Override
        public String toString() {
            return "[" + this.time + "," + this.detection + "]";
        }

        public Point2D getPos() {
            return new Point2D(detection.getX(), detection.getY());
        }
    }

    private ArrayList<SortedDetection> detections = new ArrayList<SortedDetection>();
    private Point2D vel;

    public void update(SSL_DetectionBall detection, double time) {
        SortedDetection latest = new SortedDetection(detection, time);
        detections.add(latest);
        // return when there is no previous data
        if (detections.size() == 1) {
            vel = new Point2D(0, 0);
            return;
        }           
        SortedDetection secondLatest = detections.get(detections.size() - 2); // change peek() to get(size  - 2);
        double dt = (latest.time - secondLatest.time) * 1000; 
        if (dt != 0) {
            vel = latest.getPos().subtract(secondLatest.getPos()).multiply(1 / dt);
        }
    }

    public Point2D getPos() {
        return detections.get(detections.size() - 1).getPos();
    }

    public Point2D getVel() {
        return vel;
    }
}