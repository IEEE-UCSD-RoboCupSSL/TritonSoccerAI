package SimuBot;

import java.util.List;
import Protobuf.MessagesRobocupSslDetection.SSL_DetectionBall;
import Protobuf.MessagesRobocupSslDetection.SSL_DetectionRobot;

public class DetectionType extends AbstractData {

    public static final int NUM_ROBOTS = 6;
    private static final DetectionType DETECTION = new DetectionType();

    public SSL_DetectionRobot[] blueRobots = new SSL_DetectionRobot[NUM_ROBOTS];
    public SSL_DetectionRobot[] yellowRobots = new SSL_DetectionRobot[NUM_ROBOTS];
    public SSL_DetectionBall ball;
    
    public double t_capture;
    public double t_sent;

    public DetectionType() {
    }

    public static DetectionType getInstance() {
        return DETECTION;
    }

    public void updateTime(double t_capture, double t_sent) {
        this.t_capture = t_capture;
        this.t_sent = t_sent;
    }
    
    public void updateRobot(boolean isBlue, int id, SSL_DetectionRobot robotUpdate) {
        if(isBlue) {
            blueRobots[id] = robotUpdate;
        } else {
            yellowRobots[id] = robotUpdate;
        }
    }

    public void updateRobots(boolean isBlue, List<SSL_DetectionRobot> robots) {
        for(SSL_DetectionRobot robot : robots) {
            updateRobot(isBlue, robot.getRobotId(), robot);
        }
    }

    public void updateBall(SSL_DetectionBall ball) {
        this.ball = ball;
    }

    @Override
    public String toString() {
        String s = "LAST SENT TIME: " + t_sent + " | LAST CAPTURE TIME: " + t_capture + "\n";
        s += "[[ROBOTS]]=============================\n";
        for(int i = 0; i < NUM_ROBOTS; i++) {
            SSL_DetectionRobot robot = blueRobots[i];
            s += "[BLUE ROBOT " + i + "]-------------------------\n";
            if(robot != null) {
                s += robot.toString() + "\n";
            } else {
                s += "NO INFO\n";
            }
        }
        for(int i = 0; i < NUM_ROBOTS; i++) {
            s += "[YELLOW ROBOT " + i + "]-----------------------\n";
            SSL_DetectionRobot robot = yellowRobots[i];
            if(robot != null) {
                s += robot.toString() + "\n";
            } else {
                s += "NO INFO\n";
            }
        }
        s += "[[BALL]]===============================\n" + ball.toString();
        return s;
    } 
}