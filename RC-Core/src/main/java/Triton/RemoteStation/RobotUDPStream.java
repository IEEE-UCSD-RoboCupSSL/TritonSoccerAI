package Triton.RemoteStation;

import Triton.DesignPattern.PubSubSystem.Module;

import java.net.DatagramSocket;

public abstract class RobotUDPStream implements Module {

	protected int port;
	protected int ID;

	protected DatagramSocket socket;

	public RobotUDPStream(int port, int ID) {
		this.port = port;
	}

	@Override
	public void run() {
	}
}
