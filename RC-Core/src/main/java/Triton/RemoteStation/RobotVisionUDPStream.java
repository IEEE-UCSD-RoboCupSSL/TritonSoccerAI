package Triton.RemoteStation;

import java.util.*;

import Proto.RemoteAPI.VisionData;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.Detection.BallData;
import Triton.Detection.RobotData;
import Triton.Detection.Team;

public class RobotVisionUDPStream extends RobotUDPStream {

    private Subscriber<RobotData> robotSub;
    private Subscriber<BallData> ballSub;

    public RobotVisionUDPStream(String ip, int port, Team team, int ID) {
        super(ip, port, team, ID);

        String name = (team == Team.YELLOW) ? "yellow robot data" + ID : "blue robot data" + ID;
        robotSub = new FieldSubscriber<RobotData>("detection", name);
        ballSub = new FieldSubscriber<BallData>("detection", "ball");
    }

    private void sendVision() {
        while (!robotSub.subscribe() || !ballSub.subscribe());

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
}
