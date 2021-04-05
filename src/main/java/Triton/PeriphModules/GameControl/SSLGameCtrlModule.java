package Triton.PeriphModules.GameControl;

import Proto.SslGcApi;
import java.io.ByteArrayInputStream;
import java.net.*;

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
            socket = new MulticastSocket(MC_PORT);
            InetAddress group = InetAddress.getByName(MC_ADDR);
            socket.joinGroup(group);

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



            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
