package Client;

import java.io.*;
import java.net.*;

public class UDPClient {

    public static final int UDP_PORT = 8952;
    private static byte[] buf = new byte[256];

    public static void main(String[] args) {

        while(true) {
            try {
                DatagramSocket socket = new DatagramSocket(UDP_PORT);
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println(received);
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}