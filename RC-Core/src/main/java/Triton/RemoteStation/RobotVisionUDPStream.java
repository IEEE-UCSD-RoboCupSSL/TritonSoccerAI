package Triton.RemoteStation;

import java.util.*;
import java.util.concurrent.TimeoutException;

import Proto.RemoteAPI.VisionData;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.Detection.BallData;
import Triton.Detection.RobotData;
import Triton.Detection.Team;

public class RobotVisionUDPStream extends RobotUDPStreamSend {

    private Subscriber<RobotData> robotSub;
    private Subscriber<BallData> ballSub;

    public RobotVisionUDPStream(String ip, int port, int ID) {
        super(ip, port, ID);
        robotSub = new FieldSubscriber<RobotData>("detection", "" + ID);
        ballSub = new FieldSubscriber<BallData>("detection", "ball");
    }

    private void subscribe() {
        try {
            robotSub.subscribe(1000);
            ballSub.subscribe(1000);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void sendVision() {
        RobotData robotData = robotSub.getMsg();
        BallData ballData = ballSub.getMsg();

        VisionData.Builder toSend = VisionData.newBuilder();
        toSend.setBotPos(robotData.getPos().toProto());
        toSend.setBotVel(robotData.getVel().toProto());
        toSend.setBotAng(robotData.getOrient());
        toSend.setBotAngVel(robotData.getAngularVelocity());
        toSend.setBallPos(ballData.getPos().toProto());
        toSend.setBallVel(ballData.getVel().toProto());
        byte[] bytes = toSend.build().toByteArray();
        send(bytes);
    }

    @Override
	public void run() {
        subscribe();
	}
}
