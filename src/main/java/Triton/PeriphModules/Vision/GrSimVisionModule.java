package Triton.PeriphModules.Vision;

import Proto.MessagesRobocupSslDetection.SSL_DetectionFrame;
import Proto.MessagesRobocupSslGeometry.SSL_GeometryData;
import Proto.MessagesRobocupSslWrapper.SSL_WrapperPacket;
import Triton.Config.ConnectionConfig;
import Triton.Misc.ModulePubSubSystem.MQPublisher;
import Triton.Misc.ModulePubSubSystem.Publisher;

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

/**
 * Module to receive data from grSim and send to GeometryModule and Detection Module
 */
public class GrSimVisionModule extends VisionModule {

    private final static int MAX_BUFFER_SIZE = 67108864;
    private final Publisher<SSL_DetectionFrame> detectPub;
    private final Publisher<SSL_GeometryData> geoPub;
    private MulticastSocket socket;
    private DatagramPacket packet;

    /**
     * Constructs a VisionModule listening on default ip and port inside ConnectionConfig
     */
    public GrSimVisionModule() {
        this(ConnectionConfig.GRSIM_MC_ADDR, ConnectionConfig.GRSIM_MC_PORT);
    }

    /**
     * Constructs a VisionModule listening on specified ip and port
     * @param ip ip to receive from
     * @param port port to recieve from
     */
    public GrSimVisionModule(String ip, int port) {
        detectPub = new MQPublisher<>("vision", "detection");
        geoPub = new MQPublisher<>("vision", "geometry");

        byte[] buffer = new byte[MAX_BUFFER_SIZE];

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

    /**
     * Repeatedly to collect data
     */
    @Override
    public void run() {
        try {
            while (true) {
                update();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Receive a single packet
     */
    protected void update() {
        try {
            socket.receive(packet);
            ByteArrayInputStream input = new ByteArrayInputStream(packet.getData(),
                    packet.getOffset(), packet.getLength());
            SSL_WrapperPacket SSLPacket =
                    SSL_WrapperPacket.parseFrom(input);

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

    /**
     * Receive a specified number of packets
     * @param numIter Number of packets to receive
     */
    protected void collectData(int numIter) {
        for (int i = 0; i < numIter; i++) {
            update();
        }
    }

    /**
     * Receives 4 packets
     */
    protected void collectData() {
        // default configuration with 2x6 robots need 4 packets 
        collectData(4);
    }
}