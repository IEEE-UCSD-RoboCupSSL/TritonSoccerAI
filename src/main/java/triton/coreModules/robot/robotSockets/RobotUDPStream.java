package triton.coreModules.robot.robotSockets;

import proto.RemoteAPI;
import triton.coreModules.robot.Team;
import triton.misc.modulePubSubSystem.FieldSubscriber;
import triton.misc.modulePubSubSystem.Module;
import triton.misc.modulePubSubSystem.Subscriber;
import triton.periphModules.detection.BallData;
import triton.periphModules.detection.RobotData;

import java.io.IOException;
import java.net.*;

/**
 * Implementation of UDP Stream
 */
public class RobotUDPStream implements Module {
    protected InetAddress address;
    protected DatagramSocket socket;

    protected int port, id;

    private final Subscriber<RemoteAPI.CommandData> commandsSub;
    private final Subscriber<RobotData> allySub;
    private final Subscriber<BallData> ballSub;

    private boolean isFirstRun = true;

    /**
     * Constructs a UDP stream
     *
     * @param ip ip of UDP stream
     * @param port port of UDP stream
     * @param id   ID of robot
     */
    public RobotUDPStream(String ip, int port, int id, Team myTeam) {
        this.port = port;
        this.id = id;

        commandsSub = new FieldSubscriber<>("From:Ally", "Commands " + id);
        allySub = new FieldSubscriber<>("From:DetectionModule", myTeam.name() + id);
        ballSub = new FieldSubscriber<>("From:DetectionModule", "Ball");

        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName(ip);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (isFirstRun) {
            subscribe();
            isFirstRun = false;
        }

        sendData();
    }

    /**
     * Subscribe to publishers
     */
    private void subscribe() {
        try {
            commandsSub.subscribe(1000);
            allySub.subscribe(1000);
            ballSub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendData() {
        RemoteAPI.UDPData.Builder data = RemoteAPI.UDPData.newBuilder();

        // Commands
        RemoteAPI.CommandData commandData = commandsSub.getMsg();

        // Vision
        RobotData allyData = allySub.getMsg();
        BallData ballData = ballSub.getMsg();

        RemoteAPI.VisionData.Builder visionData = RemoteAPI.VisionData.newBuilder();
        visionData.setBotPos(allyData.getPos().toProto());
        visionData.setBotVel(allyData.getVel().toProto());
        visionData.setBotAng(allyData.getDir());
        visionData.setBotAngVel(allyData.getAngleVel());
        visionData.setBallPos(ballData.getPos().toProto());
        visionData.setBallVel(ballData.getVel().toProto());

        // Combine them
        data.setCommandData(commandData);
        data.setVisionData(visionData);

        byte[] bytes;
        bytes = data.build().toByteArray();
        send(bytes);
    }

    /**
     * Sends a packet
     *
     * @param msg message to send as byte array
     */
    protected void send(byte[] msg) {
        DatagramPacket packet = new DatagramPacket(msg, msg.length, address, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
