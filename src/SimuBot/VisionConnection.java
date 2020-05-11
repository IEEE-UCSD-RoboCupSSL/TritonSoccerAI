package SimuBot;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.logging.*;
import java.util.List;
import Protobuf.*;
import Protobuf.MessagesRobocupSslDetection.*;
import Protobuf.MessagesRobocupSslGeometry.*;

public class VisionConnection implements Subject {

    final static int MAX_BUFFER_SIZE = 10000000;

    String ip;
    int port;
    byte[] buffer;
    MulticastSocket ds;
    InetAddress group;
    DatagramPacket dp;
    Logger logger;
    ArrayList<Observer> observers;
    DetectionType detection;
    GeometryType geometry;

    public void addObserver(Observer observer) {
        this.observers.add(observer);
    }

    public void deleteObserver(Observer observer) {
        this.observers.remove(observer);
    }

    public void notifyObservers() {
        for (Observer obs : this.observers) {   
            if(obs.getClassName() == "FieldDetection") {
                obs.update(detection);
            }
            if(obs.getClassName() == "FieldGeometry") {
                obs.update(geometry);
            }
        }
    }

    public VisionConnection(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.buffer = new byte[MAX_BUFFER_SIZE];
        this.logger = Logger.getLogger(VisionConnection.class.getName());
        this.detection = DetectionType.getInstance();
        this.geometry = GeometryType.getInstance();
        this.observers = new ArrayList<Observer>();

        try {
            ds = new MulticastSocket(port);
            group = InetAddress.getByName(ip);
            ds.joinGroup(this.group);
            dp = new DatagramPacket(buffer, buffer.length);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.toString());
        }
    }

    /*
     * Author: Zihao Zhou
     */
    public void receiveOnePacket() {
        try {
            this.ds.receive(dp);
            MessagesRobocupSslWrapper.SSL_WrapperPacket packet;
            packet = MessagesRobocupSslWrapper.SSL_WrapperPacket.parseFrom(trim(this.dp.getData()));
            SSL_DetectionFrame df = packet.getDetection();
            SSL_GeometryData gd = packet.getGeometry();
            

            List<SSL_DetectionRobot> yellowRobots = df.getRobotsYellowList();
            List<SSL_DetectionRobot> blueRobots = df.getRobotsBlueList();
            List<SSL_DetectionBall> balls = df.getBallsList();
            SSL_GeometryFieldSize fieldGeometry = gd.getField();
            //SSL_GeometryCameraCalibration camCali = gd.getCalibList().get(0); // simulator doesn't have cam calibration

            double t_sent    = df.getTSent();
            double t_capture = df.getTCapture();

            detection.updateRobots(false, yellowRobots);
            detection.updateRobots(true, blueRobots);
            detection.updateBall(balls.get(0)); // There should be only one ball
            detection.updateTime(t_sent, t_capture);

            geometry.updateFieldGeometry(fieldGeometry);
            //geometry.updateCameraCalibration(camCali);

            
        } catch (Exception e) {
            logger.log(Level.WARNING, e.toString());
        }

    }

    public void collectData(int numIter) {
        for (int i = 0; i < numIter; i++) {
            receiveOnePacket();
        }
        notifyObservers();
    }

    /*
     * Source: https://bit.ly/3cjWx9U
     */
    public static byte[] trim(byte[] bytes) {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0) {
            --i;
        }
        return Arrays.copyOf(bytes, i + 1);
    }
}