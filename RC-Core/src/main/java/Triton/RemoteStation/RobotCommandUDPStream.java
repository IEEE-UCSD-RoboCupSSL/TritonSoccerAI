package Triton.RemoteStation;

import Proto.RemoteAPI.Commands;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.Detection.Team;

public class RobotCommandUDPStream extends RobotUDPStream {

    private Subscriber<Commands> commandsSub;

    public RobotCommandUDPStream(String ip, int port, Team team, int ID) {
        super(ip, port, team, ID);

        commandsSub = new MQSubscriber<Commands>("commands", team.name() + ID, 1);
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
