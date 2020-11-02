package Triton.RemoteStation;

import java.io.IOException;
import java.net.*;

import Triton.DesignPattern.PubSubSystem.Module;

public abstract class RobotUDPStream implements Module {
	private static final int MAX_BUFFER_SIZE = 67108864;

	protected int port;
	protected int ID;

	protected DatagramSocket socket;

	protected byte[] buf;

	public RobotUDPStream(int port, int ID) {
		this.port = port;
		buf = new byte[MAX_BUFFER_SIZE];
	}

	@Override
	public void run() {
	}
}
