package Triton.CoreModules.Robot.RobotSockets;

import Proto.RemoteAPI.VisionData;
import Triton.Config.ObjectConfig;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Subscriber;
import Triton.PeriphModules.Detection.BallData;
import Triton.PeriphModules.Detection.RobotData;

import java.util.concurrent.TimeoutException;

/**
 * UDP stream to send vision data to robot
 */
public class RobotVisionUDPStream extends RobotUDPStreamSend {

    private final Subscriber<RobotData> allySub;
    private final Subscriber<BallData> ballSub;

    /**
     * Constructs the UDP stream
     *
     * @param ip   ip to send to
     * @param port port to send to
     * @param ID   ID of robot
     */
    public RobotVisionUDPStream(String ip, int port, Team team, int ID) {
        super(ip, port, ID);
        allySub = new FieldSubscriber<>("detection", ObjectConfig.MY_TEAM.name() + ID);
        ballSub = new FieldSubscriber<>("detection", "ball");
    }

    @Override
    public void run() {
        subscribe();
        while (true) { // delay added
            sendVision();

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
        toSend.setBotAng(allyData.getDir());
        toSend.setBotAngVel(allyData.getAngleVel());
        toSend.setBallPos(ballData.getPos().toProto());
        toSend.setBallVel(ballData.getVel().toProto());
        bytes = toSend.build().toByteArray();
        send(bytes);
    }
}
