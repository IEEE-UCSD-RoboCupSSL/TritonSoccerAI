package Triton.RemoteStation;

import java.io.*;
import java.net.*;
import java.util.*;

import Proto.RemoteCommands.*;
import Triton.Detection.*;

public class MCVision implements Runnable {

    public static final String MC_ADDR = "224.5.0.1";
    public static final int MC_PORT = 10020;
    public static final double MIN_INTERVAL = 0.001; // 1 ms

    private DatagramSocket socket;
    private InetAddress group;

    private static byte[] buf;

    public MCVision() {
        try {
            socket = new DatagramSocket();
            group = InetAddress.getByName(MC_ADDR);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static Data_Send toProto(DetectionData data) {
        Data_Send.Builder toSend = Data_Send.newBuilder();

        toSend.setBallLocation(toProto(data.getBallPos()));
        toSend.setBallVelocity(toProto(data.getBallVel()));
        
        ArrayList<Vec2D> blueRobotLocs       = new ArrayList<Vec2D>();
        ArrayList<Double> blueRobotOrients   = new ArrayList<Double>();
        ArrayList<Vec2D> blueRobotVels       = new ArrayList<Vec2D>();

        ArrayList<Vec2D> yellowRobotLocs     = new ArrayList<Vec2D>();
        ArrayList<Double> yellowRobotOrients = new ArrayList<Double>();
        ArrayList<Vec2D> yellowRobotVels     = new ArrayList<Vec2D>();


        for (int i = 0; i < DetectionData.ROBOT_COUNT; i++) {
            blueRobotLocs.add(toProto(data.getRobotPos(Team.BLUE, i)));
            blueRobotOrients.add(data.getRobotOrient(Team.BLUE, i));
            blueRobotVels.add(toProto(data.getRobotVel(Team.BLUE, i)));

            yellowRobotLocs.add(toProto(data.getRobotPos(Team.YELLOW, i)));
            yellowRobotOrients.add(data.getRobotOrient(Team.YELLOW, i));
            yellowRobotVels.add(toProto(data.getRobotVel(Team.YELLOW, i)));
        }

        toSend.addAllBlueRobotLocations(blueRobotLocs);
        toSend.addAllBlueRobotOrientations(blueRobotOrients);
        toSend.addAllBlueRobotVelocities(blueRobotVels);

        toSend.addAllYellowRobotLocations(yellowRobotLocs);
        toSend.addAllYellowRobotOrientations(yellowRobotOrients);
        toSend.addAllYellowRobotVelocities(yellowRobotVels);

        return toSend.build();
    }

    private static Vec2D toProto(Triton.Shape.Vec2D v) {
        Vec2D.Builder builder = Vec2D.newBuilder();
        builder.setX(v.x);
        builder.setY(v.y);
        return builder.build();
    }

    public void run() {
        while (true) {

            double lastTime = 0.0;

            DetectionData data;

            try {
                data = DetectionData.get();
            }  catch (NullPointerException | IndexOutOfBoundsException e) {
                continue;
            }

            try {
                // Don't update if time diff < 1ms
                if (data.getTime() - lastTime < MIN_INTERVAL) {
                    lastTime = data.getTime();
                    continue;
                }
                lastTime = data.getTime();

                Data_Send toSend = toProto(data);
                //System.out.println(toSend);

                buf = toSend.toByteArray();

                DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                pkt.setAddress(group);
                pkt.setPort(MC_PORT);

                socket.send(pkt);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}