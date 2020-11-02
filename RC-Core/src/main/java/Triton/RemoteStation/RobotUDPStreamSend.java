package Triton.RemoteStation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

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
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
