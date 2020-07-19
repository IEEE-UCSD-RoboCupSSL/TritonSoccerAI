package Triton.Vision;
import Triton.DesignPattern.*;
import Triton.Detection.DetectionManager;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.logging.*;
import java.util.List;
import java.io.ByteArrayInputStream;
import Proto.*;
import Proto.MessagesRobocupSslDetection.*;
import Proto.MessagesRobocupSslGeometry.*;

public class VisionConnection implements Subject {

    final static int MAX_BUFFER_SIZE = 10000000;

    private String ip;
    private int port;
    private byte[] buffer;
    private MulticastSocket ds;
    private InetAddress group;
    private DatagramPacket dp;
    public static Logger logger = Logger.getLogger(VisionConnection.class.getName());;
    private ArrayList<Observer> observers;
    private DetectionData detection;
    private GeometryData geometry;

    public DetectionManager dm = new DetectionManager();

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
        this.detection = DetectionData.getInstance();
        this.geometry = GeometryData.getInstance();
        this.observers = new ArrayList<Observer>();

        try {
            ds = new MulticastSocket(port);
            group = InetAddress.getByName(ip);
            ds.joinGroup(this.group);
            dp = new DatagramPacket(buffer, buffer.length);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }

    /*
     * Author: Zihao Zhou
     */
    public void receiveOnePacket() {
        try {
            this.ds.receive(dp);
            MessagesRobocupSslWrapper.SSL_WrapperPacket packet;
            ByteArrayInputStream input = new ByteArrayInputStream(dp.getData(), dp.getOffset(), dp.getLength());
            packet = MessagesRobocupSslWrapper.SSL_WrapperPacket.parseFrom(input);
            SSL_DetectionFrame df = packet.getDetection();
            SSL_GeometryData gd = packet.getGeometry();

            dm.update(df);

            /*List<SSL_DetectionRobot> yellowRobots = df.getRobotsYellowList();
            List<SSL_DetectionRobot> blueRobots = df.getRobotsBlueList();
            List<SSL_DetectionBall> balls = df.getBallsList();
            SSL_GeometryFieldSize fieldGeometry = gd.getField();
            //SSL_GeometryCameraCalibration camCali = gd.getCalibList().get(0); // simulator doesn't have cam calibration

            double t_sent    = df.getTSent();
            double t_capture = df.getTCapture();

            detection.updateRobots(false, yellowRobots);
            detection.updateRobots(true, blueRobots);

            if(balls.size() > 0) {
                detection.updateBall(balls.get(0)); // There should be only one ball
            }
            detection.updateTime(t_sent, t_capture);

            geometry.updateFieldGeometry(fieldGeometry);*/
        } catch (Exception e) {
            e.printStackTrace();
            //logger.log(Level.WARNING, e.toString());
        }

    }

    public void collectData(int numIter) {
        for (int i = 0; i < numIter; i++) {
            receiveOnePacket();
        }
        notifyObservers();
    }

    public void collectData() {
        // default configuration with 2x6 robots need 4 packets 
        collectData(4);
    }

    /*
     * preheating by looping numIter times 
     * to drain out the initial problematic 
     * data
     */
    public void preheating(int numIter) {
        for(int i = 0; i < numIter; i++) this.collectData(1);
    }
    
    public void preheating() {
        
        preheating(200); // default 200 iters
    }
}