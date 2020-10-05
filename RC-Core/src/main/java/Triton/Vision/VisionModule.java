package Triton.Vision;

import Triton.Config.ConnectionConfig;
import Triton.DesignPattern.PubSubSystem.Module;
import Triton.DesignPattern.PubSubSystem.*;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.io.ByteArrayInputStream;
import Proto.*;
import Proto.MessagesRobocupSslDetection.SSL_DetectionFrame;
import Proto.MessagesRobocupSslGeometry.SSL_GeometryData;

public class VisionModule implements Module {

    final static int MAX_BUFFER_SIZE = 67108864;

    private byte[] buffer;
    private MulticastSocket socket;
    private DatagramPacket  packet;

    public boolean geoInit = false;

    private Publisher<SSL_DetectionFrame> detectPub;
    private Publisher<SSL_GeometryData> geoPub;

    public VisionModule() {
        this(ConnectionConfig.GRSIM_MC_ADDR, ConnectionConfig.GRSIM_MC_PORT);
    }

    public VisionModule(String ip, int port) {
        detectPub = new MQPublisher<SSL_DetectionFrame>("vision", "detection");
        geoPub = new MQPublisher<SSL_GeometryData>("vision", "geometry");

        buffer = new byte[MAX_BUFFER_SIZE];
        try {
            socket = new MulticastSocket(port);
            InetSocketAddress group = new InetSocketAddress(ip, port);
            NetworkInterface netIf = NetworkInterface.getByName("bge0");
            socket.joinGroup(group, netIf);
            packet = new DatagramPacket(buffer, buffer.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            collectData();
        }
    }

    public void update() {
        try {
            socket.receive(packet);
            ByteArrayInputStream input = new ByteArrayInputStream(packet.getData(), 
                packet.getOffset(), packet.getLength());
            MessagesRobocupSslWrapper.SSL_WrapperPacket SSLPacket = 
                MessagesRobocupSslWrapper.SSL_WrapperPacket.parseFrom(input);
            
            if (SSLPacket.hasDetection()) {
                detectPub.publish(SSLPacket.getDetection());
            }

            if (SSLPacket.hasGeometry()) {
                geoPub.publish(SSLPacket.getGeometry());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void collectData(int numIter) {
        for (int i = 0; i < numIter; i++) {
            update();
        }
    }

    public void collectData() {
        // default configuration with 2x6 robots need 4 packets 
        collectData(4);
    }
}