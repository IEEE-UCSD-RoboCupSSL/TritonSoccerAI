package Triton.Modules.RemoteStation;

import Proto.RemoteAPI.VisionData;
import Triton.Dependencies.DesignPattern.PubSubSystem.FieldSubscriber;
import Triton.Dependencies.DesignPattern.PubSubSystem.Subscriber;
import Triton.Modules.Detection.BallData;
import Triton.Modules.Detection.RobotData;
import Triton.Modules.Detection.Team;

import java.util.concurrent.TimeoutException;

/**
 * UDP stream to send vision data to robot
 */
public class RobotVisionUDPStream extends RobotUDPStreamSend {

    private final Subscriber<RobotData> robotSub;
    private final Subscriber<BallData> ballSub;

    /**
     * Constructs the UDP stream
     * @param ip ip to send to
     * @param port port to send to
     * @param ID ID of robot
     */
    public RobotVisionUDPStream(String ip, int port, Team team, int ID) {
        super(ip, port, ID);
        if(team == Team.BLUE) {
            robotSub = new FieldSubscriber<>("detection", "blue robot data" + ID);
        }
        else {
            robotSub = new FieldSubscriber<>("detection", "yellow robot data" + ID);
        }
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
            robotSub.subscribe(1000);
            ballSub.subscribe(1000);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends vision data
     */
    private void sendVision() {
        RobotData robotData;
        BallData ballData;
        VisionData.Builder toSend;
        byte[] bytes;

        robotData = robotSub.getMsg();
        ballData = ballSub.getMsg();

        toSend = VisionData.newBuilder();
        toSend.setBotPos(robotData.getPos().toProto());
        toSend.setBotVel(robotData.getVel().toProto());
        toSend.setBotAng(robotData.getOrient());
        toSend.setBotAngVel(robotData.getAngularVelocity());
        toSend.setBallPos(ballData.getPos().toProto());
        toSend.setBallVel(ballData.getVel().toProto());
        bytes = toSend.build().toByteArray();
        send(bytes);
    }
}
