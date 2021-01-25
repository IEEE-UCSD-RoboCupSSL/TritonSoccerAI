package Triton.CoreModules.Robot.RobotSockets;

import Proto.RemoteAPI.Commands;
import Triton.Misc.ModulePubSubSystem.MQSubscriber;
import Triton.Misc.ModulePubSubSystem.Subscriber;

/**
 * UDP stream to send commands to robot
 */
public class RobotCommandUDPStream extends RobotUDPStreamSend {

    private final Subscriber<Commands> commandsSub;

    /**
     * Construct a RobotCommandUDPStream sending to specified ip, port, and robot ID
     *
     * @param ip   ip to send to
     * @param port port to send to
     * @param ID   ID of robot
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
        while (true) {
            sendCommand();
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

    private void sendCommand() {
        Commands command = commandsSub.getMsg();
        byte[] bytes;
        bytes = command.toByteArray();
        send(bytes);
    }
}
