package Triton.RemoteStation;

import Proto.RemoteAPI.Commands;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.Detection.Team;

public class RobotCommandUDPStream extends RobotUDPStreamSend {

    private Subscriber<Commands> commandsSub;

    public RobotCommandUDPStream(String ip, int port, int ID) {
        super(ip, port, ID);
        commandsSub = new MQSubscriber<Commands>("commands", "" + ID, 1);
    }

    public void run() {
        commandsSub.subscribe();

        while (true) {
            Commands command = commandsSub.getMsg();
            byte[] bytes = command.toByteArray();
            send(bytes);
        }
    }
}
