package Triton.RemoteStation;

import java.io.*;
import java.net.*;

import Proto.RemoteCommands.*;
import Triton.Detection.*;
import Triton.Config.ConnectionConfig;

public class MCVision implements Runnable {

    private DatagramSocket socket;
    private InetAddress group;

    private static byte[] buf;

    public MCVision() {
        try {
            socket = new DatagramSocket();
            group = InetAddress.getByName(ConnectionConfig.MC_ADDR);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            DetectionData data;
            Remote_Detection toSend;

            try {
                data = DetectionData.get();
                toSend = data.toProto();
            }  catch (NullPointerException | IndexOutOfBoundsException e) {
                continue;
            }
            
            try {
                buf = toSend.toByteArray();

                DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                pkt.setAddress(group);
                pkt.setPort(ConnectionConfig.MC_PORT);

                socket.send(pkt);
                Thread.sleep(ConnectionConfig.MC_INTERVAL); // sleep for 5 ms
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}