package Triton.RemoteStation;

import java.io.IOException;
import java.net.*;

import Triton.DesignPattern.PubSubSystem.Module;
import Triton.Detection.Team;

public class RobotUDPStream implements Module {
	private static final int MAX_BUFFER_SIZE = 67108864;

	protected String ip;
	protected int port;
	protected Team team;
	protected int ID;

	protected DatagramSocket socket;
	protected InetAddress address;

	private byte[] sendBuf;
	private byte[] receiveBuf;

	public RobotUDPStream(String ip, int port, Team team, int ID) {
		this.ip = ip;
		this.port = port;
		try {
			address = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		receiveBuf = new byte[MAX_BUFFER_SIZE];
	}

	protected byte[] receive() {
		DatagramPacket packet = new DatagramPacket(receiveBuf, receiveBuf.length);
		try {
			socket.receive(packet);
			return receiveBuf;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void send(byte[] msg) {
		DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
	}
}
