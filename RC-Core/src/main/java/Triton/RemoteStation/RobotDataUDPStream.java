package Triton.RemoteStation;

import Proto.RemoteAPI.RobotInternalData;
import Triton.DesignPattern.PubSubSystem.MQPublisher;
import Triton.DesignPattern.PubSubSystem.Publisher;

public class RobotDataUDPStream extends RobotUDPStreamReceive {

    private final Publisher<RobotInternalData> internalPub;

    public RobotDataUDPStream(int port, int ID) {
        super(port, ID);
        internalPub = new MQPublisher<>("robot", "internal" + ID);
    }

    private void receiveEKF() {
        byte[] buf = receive();
        try {
            RobotInternalData internalPub = RobotInternalData.parseFrom(buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            receiveEKF();
        }
    }
}