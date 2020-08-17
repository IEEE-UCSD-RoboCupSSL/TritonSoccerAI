package Client;

import java.io.*;
import java.net.*;
import Client.RemoteCommands.Remote_Detection;
  
public class MCClient {
    public static final String MC_ADDR = "224.5.0.1";
    public static final int MC_PORT = 10020;

    protected static MulticastSocket socket = null;
    protected static byte[] buf = new byte[1024];

    public static void main(String[] args) {
        try {
            InetAddress mcAddr = InetAddress.getByName(MC_ADDR);
            InetSocketAddress group = new InetSocketAddress(mcAddr, MC_PORT);
            NetworkInterface netIf = NetworkInterface.getByName("bge0");
            socket = new MulticastSocket(MC_PORT);
            socket.joinGroup(group, netIf);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true) {
            DatagramPacket pkt = new DatagramPacket(buf, buf.length);

            try {
                socket.receive(pkt);
                ByteArrayInputStream input = new ByteArrayInputStream(pkt.getData(), pkt.getOffset(), pkt.getLength());
                Remote_Detection received = Remote_Detection.parseFrom(input);
                System.out.println(received);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}