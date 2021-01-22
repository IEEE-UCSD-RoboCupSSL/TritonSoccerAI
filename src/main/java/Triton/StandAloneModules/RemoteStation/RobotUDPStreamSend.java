package Triton.StandAloneModules.RemoteStation;

import java.io.IOException;
import java.net.*;

/**
 * UDP stream to send packets to robot
 */
public class RobotUDPStreamSend extends RobotUDPStream {
    protected InetAddress address;

    /**
     * Constructs the UDP stream
     * @param ip ip to send to
     * @param port port to send to
     * @param ID ID of robot
     */
    public RobotUDPStreamSend(String ip, int port, int ID) {
        super(port, ID);
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName(ip);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a packet
     * @param msg message to send as byte array
     */
    protected void send(byte[] msg) {
		DatagramPacket packet = new DatagramPacket(msg, msg.length, address, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
