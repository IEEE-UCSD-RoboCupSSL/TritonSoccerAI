package Triton.RemoteStation;

import Triton.DesignPattern.PubSubSystem.Module;

public class RobotUDPStream implements Module {
	private String ip;
	private int port;

    public RobotUDPStream(String ip, int port) {
		this.ip = ip;
		this.port = port;
    }

	@Override
	public void run() {
	}
}
