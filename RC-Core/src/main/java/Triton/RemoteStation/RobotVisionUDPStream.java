package Triton.RemoteStation;

import Proto.RemoteAPI.VisionData;
import Triton.DesignPattern.PubSubSystem.FieldSubscriber;
import Triton.DesignPattern.PubSubSystem.Subscriber;
import Triton.Detection.BallData;
import Triton.Detection.RobotData;

import java.util.concurrent.TimeoutException;

public class RobotVisionUDPStream extends RobotUDPStreamSend {

    private final Subscriber<RobotData> robotSub;
    private final Subscriber<BallData> ballSub;

    public RobotVisionUDPStream(String ip, int port, int ID) {
        super(ip, port, ID);
        robotSub = new FieldSubscriber<>("detection", "" + ID);
        ballSub = new FieldSubscriber<>("detection", "ball");
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
