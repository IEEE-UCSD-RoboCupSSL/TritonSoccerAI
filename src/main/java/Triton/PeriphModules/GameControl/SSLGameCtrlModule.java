package Triton.PeriphModules.GameControl;

import Proto.SslGcApi;
import Proto.SslGcRefereeMessage;
import Triton.Config.Config;
import Triton.Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;

public class SSLGameCtrlModule extends GameCtrlModule {
    private final static int MAX_BUFFER_SIZE = 67108864;

    private DatagramSocket socket;
    private DatagramPacket packet;

    public SSLGameCtrlModule() {
        super("ssl game controller");

        byte[] buffer = new byte[MAX_BUFFER_SIZE];

        NetworkInterface netIf = Util.getNetIf(Config.conn().getGcNetIf());
        socket = Util.mcSocket(Config.conn().getGcMcAddr(),
                Config.conn().getGcMcPort(),
                netIf);
        packet = new DatagramPacket(buffer, buffer.length);
    }

    @Override
    public void run() {
        while (true) {
            try {
                socket.receive(packet);
                ByteArrayInputStream input = new ByteArrayInputStream(packet.getData(),
                        packet.getOffset(), packet.getLength());

                SslGcRefereeMessage.Referee gcOutput = SslGcRefereeMessage.Referee.parseFrom(input);
                System.out.println(gcOutput);
                parseGcOutput(gcOutput);

            } catch (SocketTimeoutException e) {
                System.err.println("SSL Game Controller Multicast Timeout");
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private void parseGcOutput(SslGcRefereeMessage.Referee gcOutput) {

    }
}
