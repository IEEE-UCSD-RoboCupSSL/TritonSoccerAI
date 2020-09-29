package Triton.RemoteStation;

import java.util.*;

import Proto.RemoteCommands.Command;
import Triton.DesignPattern.PubSubSystem.Subscriber;
import Triton.Detection.Team;
import Triton.Shape.*;

public class RobotCommandUDPStream extends RobotUDPStream {

    private Subscriber<Command> commandSub;

    public RobotCommandUDPStream(String ip, int port, Team team, int ID) {
        super(ip, port, team, ID);

        commandSub = new Subscriber<Command>("command", team.name() + ID, 1);
    }

    private void sendCommand() {
        Command command = commandSub.pollMsg();
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
