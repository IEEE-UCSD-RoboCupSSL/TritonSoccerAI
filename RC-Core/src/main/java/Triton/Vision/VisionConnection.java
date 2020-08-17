package Triton.Vision;

import Triton.Config.ConnectionConfig;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.io.ByteArrayInputStream;
import Proto.*;

public class VisionConnection implements Runnable {

    final static int MAX_BUFFER_SIZE = 67108864;

    private byte[] buffer;
    private MulticastSocket socket;
    private DatagramPacket  packet;

    private VisionData vision = new VisionData();;

    public boolean geoInit = false;

    public VisionConnection() {
        this(ConnectionConfig.GRSIM_MC_ADDR, ConnectionConfig.GRSIM_MC_PORT);
    }

    public VisionConnection(String ip, int port) {
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

    public void update() {
        try {
            socket.receive(packet);
            ByteArrayInputStream input = new ByteArrayInputStream(packet.getData(), 
                packet.getOffset(), packet.getLength());
            MessagesRobocupSslWrapper.SSL_WrapperPacket SSLPacket = 
                MessagesRobocupSslWrapper.SSL_WrapperPacket.parseFrom(input);
            
            vision.setDetection(SSLPacket.getDetection());
            vision.setGeometry(SSLPacket.getGeometry());
            vision.publish();
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

    public void run() {
        while (true) {
            collectData();
        }
    }
}