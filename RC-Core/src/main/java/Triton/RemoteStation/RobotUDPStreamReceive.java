package Triton.RemoteStation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class RobotUDPStreamReceive extends RobotUDPStream {

    private static final int MAX_BUFFER_SIZE = 67108864;

    private final byte[] buf;

    public RobotUDPStreamReceive(int port, int ID) {
        super(port, ID);
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
		buf = new byte[MAX_BUFFER_SIZE];
    }

    protected byte[] receive() {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
            return buf;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
