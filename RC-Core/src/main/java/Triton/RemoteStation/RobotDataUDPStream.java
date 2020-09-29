package Triton.RemoteStation;

import java.util.*;

import Proto.RemoteCommands.EKF_Data;
import Triton.DesignPattern.PubSubSystem.Publisher;
import Triton.Detection.Team;
import Triton.Shape.*;

public class RobotDataUDPStream extends RobotUDPStream {

    private Publisher<EKF_Data> ekfPub;

    public RobotDataUDPStream(String ip, int port, Team team, int ID) {
        super(ip, port, team, ID);

        ekfPub = new Publisher<EKF_Data>("robot", "ekf" + team.name() + ID);
    }

    private void receiveEKF() {
        byte[] buf = receive();
        try {
            EKF_Data ekfData = EKF_Data.parseFrom(buf);
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