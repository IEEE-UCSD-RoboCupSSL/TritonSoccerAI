package Triton.CoreModules.Robot.RobotSockets;

import Proto.RemoteAPI.RobotInternalData;
import Triton.Misc.DesignPattern.PubSubSystem.MQPublisher;
import Triton.Misc.DesignPattern.PubSubSystem.Publisher;

/**
 * UDP stream to receive robot internal data
 */
public class RobotDataUDPStream extends RobotUDPStreamReceive {

    private final Publisher<RobotInternalData> internalPub;
    private RobotInternalData internalData;

    /**
     * Construct a RobotDataUDPStream
     * @param port port to listen
     * @param ID ID of the robot
     */
    public RobotDataUDPStream(int port, int ID) {
        super(port, ID);
        internalPub = new MQPublisher<>("robot", "internal" + ID);
    }

    /**
     * Repeatedly receives robot EKF data
     */
    public void run() {
        while (true) {
            receiveEKF();
            internalPub.publish(internalData);
        }
    }

    /**
     * Receives EKF data
     */
    private void receiveEKF() {
        byte[] buf = receive();
        try {
            internalData = RobotInternalData.parseFrom(buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}