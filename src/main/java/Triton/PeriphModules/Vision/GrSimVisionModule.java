package Triton.PeriphModules.Vision;

import Triton.Legacy.OldGrSimProto.protosrcs.MessagesRobocupSslDetection.SSL_DetectionFrame;
import Triton.Legacy.OldGrSimProto.protosrcs.MessagesRobocupSslWrapper.SSL_WrapperPacket;
import Triton.Config.OldConfigs.jsonConfig;
import Triton.Misc.ModulePubSubSystem.MQPublisher;
import Triton.Misc.ModulePubSubSystem.Publisher;
import Triton.Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;

/**
 * Module to receive data from grSim and send to GeometryModule and Detection Module
 */
public class GrSimVisionModule extends VisionModule {

    private final static int MAX_BUFFER_SIZE = 67108864;
    private final Publisher<SSL_DetectionFrame> visionPub;
    private DatagramSocket socket;
    private DatagramPacket packet;

    /**
     * Constructs a VisionModule listening on default ip and port inside ConnectionjsonConfig
     */
    public GrSimVisionModule() {
        this(jsonConfig.conn().getGrsimMcAddr(), jsonConfig.conn().getGrsimMcPort());
    }

    /**
     * Constructs a VisionModule listening on specified ip and port
     *
     * @param ip   ip to receive from
     * @param port port to receive from
     */
    public GrSimVisionModule(String ip, int port) {
        visionPub = new MQPublisher<>("vision", "detection");

        byte[] buffer = new byte[MAX_BUFFER_SIZE];

        try {
            NetworkInterface netIf = Util.getNetIf(jsonConfig.conn().getGrsimNetIf());
            socket = Util.mcSocket(jsonConfig.conn().getGrsimMcAddr(),
                    jsonConfig.conn().getGrsimMcPort(),
                    netIf);
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
            update();
        } catch (SocketTimeoutException e) {
            System.err.println("GrSim Vision Multicast Timeout");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * Receive a single packet
     */
    protected void update() throws IOException {
        socket.receive(packet);
        ByteArrayInputStream input = new ByteArrayInputStream(packet.getData(),
                packet.getOffset(), packet.getLength());
        SSL_WrapperPacket SSLPacket =
                SSL_WrapperPacket.parseFrom(input);

        if (SSLPacket.hasDetection()) {
            visionPub.publish(SSLPacket.getDetection());
        }
    }
}