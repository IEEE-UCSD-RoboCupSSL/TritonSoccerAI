package Triton.Vision;
import Triton.Detection.DetectionManager;
import Triton.Geometry.GeometryManager;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.io.ByteArrayInputStream;
import Proto.*;
import Proto.MessagesRobocupSslDetection.*;
import Proto.MessagesRobocupSslGeometry.*;

public class VisionConnection {

    final static int MAX_BUFFER_SIZE = 10000000;

    private byte[] buffer;
    private MulticastSocket ds;
    private DatagramPacket dp;

    public DetectionManager dm = new DetectionManager();
    public GeometryManager gm = new GeometryManager();

    public boolean geoInit = false;

    public VisionConnection(String ip, int port) {
        this.buffer = new byte[MAX_BUFFER_SIZE];

        try {
            ds = new MulticastSocket(port);
            InetSocketAddress group = new InetSocketAddress(ip, port);
            NetworkInterface netIf = NetworkInterface.getByName("bge0");
            ds.joinGroup(group, netIf);
            dp = new DatagramPacket(buffer, buffer.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiveOnePacket() {
        try {
            this.ds.receive(dp);
            MessagesRobocupSslWrapper.SSL_WrapperPacket packet;
            ByteArrayInputStream input = new ByteArrayInputStream(dp.getData(), dp.getOffset(), dp.getLength());
            packet = MessagesRobocupSslWrapper.SSL_WrapperPacket.parseFrom(input);
            SSL_DetectionFrame df = packet.getDetection();
            SSL_GeometryData gd = packet.getGeometry();

            dm.update(df);
            if(!geoInit) geoInit = gm.init(gd);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void collectData(int numIter) {
        for (int i = 0; i < numIter; i++) {
            receiveOnePacket();
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
    
    public void preheating() {
        preheating(200); // default 200 iters
    }
}