package Triton.RemoteStation;

import Proto.RemoteAPI.Commands;
import Triton.DesignPattern.PubSubSystem.MQSubscriber;
import Triton.DesignPattern.PubSubSystem.Subscriber;

public class RobotCommandUDPStream extends RobotUDPStreamSend {

    private final Subscriber<Commands> commandsSub;

    public RobotCommandUDPStream(String ip, int port, int ID) {
        super(ip, port, ID);
        commandsSub = new MQSubscriber<>("commands", "" + ID, 10);
    }

    public void run() {
        subscribe();

        while (true) {
            Commands command = commandsSub.getMsg();
            byte[] bytes = command.toByteArray();
            send(bytes);
        }
    }

    private void subscribe() {
        try {
            commandsSub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
