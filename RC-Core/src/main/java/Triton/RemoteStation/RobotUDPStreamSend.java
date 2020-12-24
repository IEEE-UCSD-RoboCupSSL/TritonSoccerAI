package Triton.RemoteStation;

import java.io.IOException;
import java.net.*;

public class RobotUDPStreamSend extends RobotUDPStream {
    protected InetAddress address;

    public RobotUDPStreamSend(String ip, int port, int ID) {
        super(port, ID);
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName(ip);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }
    protected void send(byte[] msg) {
		DatagramPacket packet = new DatagramPacket(msg, msg.length, address, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
