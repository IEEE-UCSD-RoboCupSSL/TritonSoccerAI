package Triton.Detection;

import Triton.DesignPattern.MsgChannel;
import Triton.Vision.VisionData;
import Proto.MessagesRobocupSslDetection.SSL_DetectionFrame;
import Proto.MessagesRobocupSslDetection.SSL_DetectionRobot;

public class DetectionPublisher implements Runnable {

    DetectionData detect = new DetectionData();

    public void run() {
        while (true) {
            try {
                update(VisionData.get().getDetection());
                detect.publish();
            } catch (Exception e) {
                //Do nothing
            }
        }
    }

    public void update(SSL_DetectionFrame df) {
        double time = df.getTCapture();
        detect.setBallCount(df.getBallsCount());
        if (detect.getBallCount() > 0) {
            detect.updateBall(df.getBalls(0), time);
        }
        detect.updateTime(time);
        for (SSL_DetectionRobot r : df.getRobotsYellowList()) {
            detect.updateRobot(Team.YELLOW, r.getRobotId(), r, time);
        }
        for (SSL_DetectionRobot r : df.getRobotsBlueList()) {
            detect.updateRobot(Team.BLUE, r.getRobotId(), r, time);
        }
    }
}