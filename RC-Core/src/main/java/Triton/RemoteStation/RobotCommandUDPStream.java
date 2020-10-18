package Triton.RemoteStation;

import Proto.RemoteAPI.Commands;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.Detection.Team;

public class RobotCommandUDPStream extends RobotUDPStream {

    private Subscriber<Commands> commandSub;

    public RobotCommandUDPStream(String ip, int port, Team team, int ID) {
        super(ip, port, team, ID);

        commandSub = new MQSubscriber<Commands>("command", team.name() + ID, 1);
    }

    private void sendCommand() {
        Commands command = commandSub.getMsg();
        byte[] bytes = command.toByteArray();
        send(bytes);
    }

    public void run() {
        while (!commandSub.subscribe());

        while (true) {
            sendCommand();
        }
    }
}
