package Triton.Modules.RemoteStation;

import Proto.RemoteAPI.VisionData;
import Triton.Config.ObjectConfig;
import Triton.Dependencies.DesignPattern.PubSubSystem.FieldSubscriber;
import Triton.Dependencies.DesignPattern.PubSubSystem.Subscriber;
import Triton.Modules.Detection.BallData;
import Triton.Modules.Detection.RobotData;
import Triton.Dependencies.Team;

import java.util.concurrent.TimeoutException;

/**
 * UDP stream to send vision data to robot
 */
public class RobotVisionUDPStream extends RobotUDPStreamSend {

    private final Subscriber<RobotData> allySub;
    private final Subscriber<BallData> ballSub;

    /**
     * Constructs the UDP stream
     * @param ip ip to send to
     * @param port port to send to
     * @param ID ID of robot
     */
    public RobotVisionUDPStream(String ip, int port, Team team, int ID) {
        super(ip, port, ID);
        allySub = new FieldSubscriber<>("detection", ObjectConfig.MY_TEAM.name() + ID);
        ballSub = new FieldSubscriber<>("detection", "ball");
    }

    @Override
	public void run() {
        subscribe();
        while(true) {
            sendVision();
        }
	}

    /**
     * Subscribe to publishers
     */
    private void subscribe() {
        try {
            allySub.subscribe(1000);
            ballSub.subscribe(1000);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends vision data
     */
    private void sendVision() {
        RobotData allyData = allySub.getMsg();
        BallData ballData = ballSub.getMsg();

        byte[] bytes;
        VisionData.Builder toSend = VisionData.newBuilder();
        toSend.setBotPos(allyData.getPos().toProto());
        toSend.setBotVel(allyData.getVel().toProto());
        toSend.setBotAng(allyData.getAngle());
        toSend.setBotAngVel(allyData.getAngleVel());
        toSend.setBallPos(ballData.getPos().toProto());
        toSend.setBallVel(ballData.getVel().toProto());
        bytes = toSend.build().toByteArray();
        send(bytes);
    }
}
