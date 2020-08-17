package Client;

import java.io.*;
import java.net.*;
import Client.RemoteCommands.Move_To;

public class UDPClient {

    public static final int UDP_PORT = 8952;
    private static byte[] buf = new byte[256];

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(UDP_PORT);
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                ByteArrayInputStream input = new ByteArrayInputStream(packet.getData(), packet.getOffset(),
                        packet.getLength());
                System.out.println(Move_To.parseFrom(input));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}