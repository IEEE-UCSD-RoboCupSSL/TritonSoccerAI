package Triton.RemoteStation;

import java.util.*;

import Proto.RemoteAPI.VisionData;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.Detection.BallData;
import Triton.Detection.RobotData;
import Triton.Detection.Team;

public class RobotVisionUDPStream extends RobotUDPStream {

    private Subscriber<HashMap<Team, HashMap<Integer, RobotData>>> robotSub;
    private Subscriber<BallData> ballSub;

    public RobotVisionUDPStream(String ip, int port, Team team, int ID) {
        super(ip, port, team, ID);

        robotSub = new MQSubscriber<HashMap<Team, HashMap<Integer, RobotData>>>("detection", "robot", 1);
        ballSub = new MQSubscriber<BallData>("detection", "ball", 1);
    }

    private void sendVision() {
        while (!robotSub.subscribe() || !ballSub.subscribe());

        HashMap<Team, HashMap<Integer, RobotData>> robotDataMap = robotSub.getMsg();
        BallData ballData = ballSub.getMsg();
        RobotData botData = robotDataMap.get(team).get(ID);

        VisionData.Builder toSend = VisionData.newBuilder();
        toSend.setBotPos(botData.getPos().toProto());
        toSend.setBotVel(botData.getVel().toProto());
        toSend.setBotAng(botData.getOrient());
        toSend.setBotAngVel(botData.getAngularVelocity());
        toSend.setBallPos(ballData.getPos().toProto());
        toSend.setBallVel(ballData.getVel().toProto());
        byte[] bytes = toSend.build().toByteArray();
        send(bytes);
    }
}
