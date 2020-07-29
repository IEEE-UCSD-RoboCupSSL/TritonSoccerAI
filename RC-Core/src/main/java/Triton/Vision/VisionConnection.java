package Triton.Vision;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.io.ByteArrayInputStream;
import Proto.*;

public class VisionConnection implements Runnable {

    final static int MAX_BUFFER_SIZE = 10000000;

    private byte[] buffer;
    private MulticastSocket socket;
    private DatagramPacket  packet;

    private VisionData vision;

    public boolean geoInit = false;

    public VisionConnection(String ip, int port) {
        vision = new VisionData();
        VisionData.publish(vision);        

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
            this.socket.receive(packet);
            ByteArrayInputStream input = new ByteArrayInputStream(packet.getData(), 
                packet.getOffset(), packet.getLength());
            MessagesRobocupSslWrapper.SSL_WrapperPacket SSLPacket = 
                MessagesRobocupSslWrapper.SSL_WrapperPacket.parseFrom(input);
            
            vision.setDetection(SSLPacket.getDetection());
            vision.setGeometry (SSLPacket.getGeometry());
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

    /*
     * preheating by looping numIter times 
     * to drain out the initial problematic 
     * data
     */
    public void preheating(int numIter) {
        for(int i = 0; i < numIter; i++) {
            this.collectData(1);
            System.out.println("preheating " + i + "...");
        }
    }

    public void run() {
        while (true) {
            this.collectData();
        }
    }
}