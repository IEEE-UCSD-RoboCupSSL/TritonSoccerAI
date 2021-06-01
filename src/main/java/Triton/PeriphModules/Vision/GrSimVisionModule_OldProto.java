package Triton.PeriphModules.Vision;

import Triton.Config.Config;
import Triton.Legacy.OldGrSimProto.protosrcs.MessagesRobocupSslDetection.SSL_DetectionFrame;
import Triton.Legacy.OldGrSimProto.protosrcs.MessagesRobocupSslWrapper.SSL_WrapperPacket;
import Triton.Misc.ModulePubSubSystem.MQPublisher;
import Triton.Misc.ModulePubSubSystem.Publisher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;

/**
 * Module to receive data from grSim and send to GeometryModule and Detection Module
 */
public class GrSimVisionModule_OldProto extends VisionModule {

    private final static int MAX_BUFFER_SIZE = 67108864;
    private final Publisher<SSL_DetectionFrame> visionPub;
    private MulticastSocket socket;
    private DatagramPacket packet;

    /**
     * Constructs a VisionModule listening on default ip and port inside ConnectionjsonConfig
     */
    public GrSimVisionModule_OldProto(Config config) {
        this(config.connConfig.sslVisionConn.ipAddr, config.connConfig.sslVisionConn.port);
    }

    /**
     * Constructs a VisionModule listening on specified ip and port
     *
     * @param ip   ip to receive from
     * @param port port to receive from
     */
    public GrSimVisionModule_OldProto(String ip, int port) {
        visionPub = new MQPublisher<>("vision", "detection");

        byte[] buffer = new byte[MAX_BUFFER_SIZE];

        try {
//            socket = Util.mcSocket(jsonConfig.conn().getGrsimMcAddr(),
//                    jsonConfig.conn().getGrsimMcPort());

              socket = new MulticastSocket(port); // this constructor will automatically enable reuse_addr
              socket.joinGroup(new InetSocketAddress(ip, port),
                        NetworkInterface.getByInetAddress(InetAddress.getByName(ip)));

              packet = new DatagramPacket(buffer, buffer.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Receive a single packet, and publish it to proper subscribers
     */
    @Override
    protected void update() throws IOException {
        socket.receive(packet);
        ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(),
                packet.getOffset(), packet.getLength());
        SSL_WrapperPacket SSLPacket =
                SSL_WrapperPacket.parseFrom(bais);

        if (SSLPacket.hasDetection()) {
            visionPub.publish(SSLPacket.getDetection());
        }
    }
}