package Triton.RemoteStation;

import Proto.RemoteAPI.RobotInternalData;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.Detection.Team;

public class RobotInternalUDPStream extends RobotUDPStream {

    private Publisher<RobotInternalData> internalPub;

    public RobotInternalUDPStream(String ip, int port, Team team, int ID) {
        super(ip, port, team, ID);

        internalPub = new MQPublisher<RobotInternalData>("robot", "internal" + team.name() + ID);
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