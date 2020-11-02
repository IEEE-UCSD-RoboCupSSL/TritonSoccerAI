package Triton.RemoteStation;

import Proto.RemoteAPI.Commands;
import Proto.RemoteAPI.Vec3D;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.Detection.Team;

public class RobotCommandUDPStream extends RobotUDPStreamSend {

    private Subscriber<Commands> commandsSub;

    public RobotCommandUDPStream(String ip, int port, int ID) {
        super(ip, 6601, ID);
        //super(ip, port, ID);
        commandsSub = new MQSubscriber<Commands>("commands", "" + ID, 1);
    }

    public void run() {
        commandsSub.subscribe();

        while (true) {
            Commands.Builder command = Commands.newBuilder();
            command.setMode(0);
            Vec3D.Builder dest = Vec3D.newBuilder();
            dest.setX(0);
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
