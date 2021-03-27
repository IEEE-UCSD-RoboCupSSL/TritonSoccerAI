package Triton.PeriphModules.GameControl;

import Proto.SslGcApi;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class SSLGameCtrlModule extends GameCtrlModule {
    private final static String MC_ADDR = "224.5.23.2";
    private final static int MC_PORT = 10003;
    private final static int MAX_BUFFER_SIZE = 67108864;
    private MulticastSocket socket;
    private DatagramPacket packet;

    public SSLGameCtrlModule() {
        super("ssl game controller");

        byte[] buffer = new byte[MAX_BUFFER_SIZE];

        try {
            socket = new MulticastSocket();
            InetSocketAddress group = new InetSocketAddress(MC_ADDR, MC_PORT);
            NetworkInterface netIf = NetworkInterface.getByName("bge0");
            socket.joinGroup(group, netIf);
            packet = new DatagramPacket(buffer, buffer.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                socket.receive(packet);
                ByteArrayInputStream input = new ByteArrayInputStream(packet.getData(),
                        packet.getOffset(), packet.getLength());

                SslGcApi.Output gcOutput =
                        SslGcApi.Output.parseFrom(input);

                System.out.println(gcOutput);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
