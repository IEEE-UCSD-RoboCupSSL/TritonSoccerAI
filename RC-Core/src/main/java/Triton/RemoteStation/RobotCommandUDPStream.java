package Triton.RemoteStation;

import java.util.concurrent.TimeoutException;

import Proto.RemoteAPI.Commands;
import Proto.RemoteAPI.Vec3D;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.Detection.Team;

public class RobotCommandUDPStream extends RobotUDPStreamSend {

    private Subscriber<Commands> commandsSub;

    public RobotCommandUDPStream(String ip, int port, int ID) {
        super(ip, port, ID);
        commandsSub = new MQSubscriber<Commands>("commands", "" + ID, 1);
    }

    public void run() {
        try {
            commandsSub.subscribe(1000);
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        while (true) {
            Commands.Builder command = Commands.newBuilder();
            command.setMode(0);
            command.setIsWorldFrame(true);
            Vec3D.Builder dest = Vec3D.newBuilder();
            dest.setX(1000);
            dest.setY(0);
            dest.setZ(0);
            command.setMotionSetPoint(dest);
            byte[] bytes = command.build().toByteArray();
            send(bytes);
        }

        /*
        while (true) {
            Commands command = commandsSub.getMsg();
            byte[] bytes = command.toByteArray();
            send(bytes);
        }
        */
    }
}
