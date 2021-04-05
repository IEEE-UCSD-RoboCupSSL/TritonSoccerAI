package Triton.PeriphModules.GameControl;

import Proto.SslGcApi;
import Triton.Config.Config;

import java.io.ByteArrayInputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NetworkChannel;
import java.util.Collections;
import java.util.Enumeration;

public class SSLGameCtrlModule extends GameCtrlModule {
    private final static int MAX_BUFFER_SIZE = 67108864;

    private DatagramSocket socket;
    private DatagramPacket packet;

    public SSLGameCtrlModule() {
        super("ssl game controller");

        byte[] buffer = new byte[MAX_BUFFER_SIZE];

        try {
            final Enumeration<NetworkInterface> netIfs = NetworkInterface.getNetworkInterfaces();

            NetworkInterface netIf = null;
            while (netIfs.hasMoreElements()) {
                netIf = netIfs.nextElement();
                if (netIf.getDisplayName().startsWith("en")) { // use ethernet
                    break;
                }
            } // otherwise, use the last network interface

            DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)
                    .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                    .bind(new InetSocketAddress(Config.conn().getGcMcPort()))
                    .setOption(StandardSocketOptions.IP_MULTICAST_IF, netIf);

            InetAddress group = InetAddress.getByName(Config.conn().getGcMcAddr());
            dc.join(group, netIf);

            socket = dc.socket();
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

                SslGcApi.Output gcOutput = SslGcApi.Output.parseFrom(input);
                System.out.println(gcOutput);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
