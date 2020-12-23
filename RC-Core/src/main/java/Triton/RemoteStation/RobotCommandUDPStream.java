package Triton.RemoteStation;

import java.util.concurrent.TimeoutException;

import Proto.RemoteAPI.Commands;
import Triton.DesignPattern.PubSubSystem.*;

public class RobotCommandUDPStream extends RobotUDPStreamSend {

    private Subscriber<Commands> commandsSub;

    public RobotCommandUDPStream(String ip, int port, int ID) {
        super(ip, port, ID);
        commandsSub = new MQSubscriber<Commands>("commands", "" + ID, 10);
    }

    private void subscribe() {
        try {
            commandsSub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        subscribe();

        while (true) {
            Commands command = commandsSub.getMsg();
            byte[] bytes = command.toByteArray();
            send(bytes);
        }
    }
}
