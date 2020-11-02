package Triton.RemoteStation;

import java.io.IOException;
import java.net.*;

import Triton.DesignPattern.PubSubSystem.Module;

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
