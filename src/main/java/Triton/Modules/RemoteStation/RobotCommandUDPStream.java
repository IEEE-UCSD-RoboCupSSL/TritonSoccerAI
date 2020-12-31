package Triton.Modules.RemoteStation;

import Proto.RemoteAPI.Commands;
import Triton.Dependencies.DesignPattern.PubSubSystem.MQSubscriber;
import Triton.Dependencies.DesignPattern.PubSubSystem.Subscriber;

/**
 * UDP stream to send commands to robot
 */
public class RobotCommandUDPStream extends RobotUDPStreamSend {

    private final Subscriber<Commands> commandsSub;

    /**
     * Construct a RobotCommandUDPStream sending to specified ip, port, and robot ID
     * @param ip ip to send to
     * @param port port to send to
     * @param ID ID of robot
     */
    public RobotCommandUDPStream(String ip, int port, int ID) {
        super(ip, port, ID);
        commandsSub = new MQSubscriber<>("commands", "" + ID, 10);
    }

    /**
     * Repeatedly sends commands from command subscriber
     */
    @Override
    public void run() {
        subscribe();

        Commands command;
        byte[] bytes;
        while (true) {
            command = commandsSub.getMsg();
            bytes = command.toByteArray();
            send(bytes);
        }
    }

    /**
     * Subscribe to publishers
     */
    private void subscribe() {
        try {
            commandsSub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
