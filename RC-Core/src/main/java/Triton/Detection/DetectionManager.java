package Triton.Detection;

import Triton.Vision.VisionData;
import Proto.MessagesRobocupSslDetection.SSL_DetectionFrame;
import Proto.MessagesRobocupSslDetection.SSL_DetectionRobot;

public class DetectionManager implements Runnable {

    public void run() {
        while (true) {
            try {
                VisionData vision = VisionData.get();
                if (vision == null) {
                    System.out.println("Vision Null");
                } else {
                    update(vision.getDetection());
                }
            } catch (Exception e) {
                // Do nothing
            }
        }
    }

    public void update(SSL_DetectionFrame df) {
        DetectionData detect = new DetectionData();
        double time = df.getTCapture();
        
        for (SSL_DetectionRobot r : df.getRobotsYellowList()) {
            detect.updateRobot(Team.YELLOW, r.getRobotId(), r, time);
        }
        for (SSL_DetectionRobot r : df.getRobotsBlueList()) {
            detect.updateRobot(Team.BLUE,   r.getRobotId(), r, time);
        }
        detect.setBallCount(df.getBallsCount());
        if (detect.getBallCount() > 0) {
            detect.updateBall(df.getBalls(0), time);
        }
        DetectionData.publish(detect);
    }
}