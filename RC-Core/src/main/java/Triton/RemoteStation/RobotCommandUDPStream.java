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

    public void run() {
        try {
            commandsSub.subscribe(1000);
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /*
        while (true) {
            Commands.Builder command = Commands.newBuilder();
            Vec3D.Builder dest = Vec3D.newBuilder();
            dest.setX(1000);
            command.setMotionSetPoint(dest.build());
            byte[] bytes = command.build().toByteArray();
            send(bytes);
        }
        */

        while (true) {
            Commands command = commandsSub.getMsg();
            byte[] bytes = command.toByteArray();
            send(bytes);
        }
    }
}
