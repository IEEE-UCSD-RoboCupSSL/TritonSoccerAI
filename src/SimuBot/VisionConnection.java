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

    public void addObserver(Observer observer) {
        this.observers.add(observer);
    }

    public void deleteObserver(Observer observer) {
        this.observers.remove(observer);
    }

    public void notifyObservers() {
        for (Observer obs : this.observers) {
            obs.update(detection);
        }
    }

    public VisionConnection(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.buffer = new byte[MAX_BUFFER_SIZE];
        this.logger = Logger.getLogger(VisionConnection.class.getName());
        this.detection = DetectionType.getInstance();
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

            List<SSL_DetectionRobot> yellowRobots = df.getRobotsYellowList();
            List<SSL_DetectionRobot> blueRobots = df.getRobotsBlueList();
            List<SSL_DetectionBall> balls = df.getBallsList();

            detection.updateRobots(false, yellowRobots);
            detection.updateRobots(true, blueRobots);
            detection.updateBall(balls.get(0)); // There should be only one ball
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