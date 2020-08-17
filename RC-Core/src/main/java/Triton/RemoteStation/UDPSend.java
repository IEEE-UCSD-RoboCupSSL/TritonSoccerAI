package Triton.RemoteStation;

import Triton.Shape.Vec2D;

import java.util.Scanner;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import Proto.RemoteCommands.Move_To;

public class UDPSend implements Runnable {

    private StationData data;
    private DatagramSocket socket;

    public UDPSend() {
        while (true) {
            try {
                data = StationData.get();
                break;
            } catch (NullPointerException e) {
                // do nothing 
            }
        }
        try {
            socket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Scanner s = new Scanner(System.in);
        while (true) {
            System.out.println("Enter a MoveTo Command: (format: ID DEST_LOC DEST_VEL)");
            int id = s.nextInt();
            double desX = s.nextDouble();
            double desY = s.nextDouble();
            double velX = s.nextDouble();
            double velY = s.nextDouble();
            send(id, desX, desY, velX, velY);
        }
    }

    public void send(int id, double desX, double desY, double velX, double velY) {
        int port = data.getPort(id);
        
        Move_To.Builder toSend = Move_To.newBuilder();
        toSend.setDest(new Vec2D(desX, desY).toProto());
        toSend.setVel(new Vec2D(velX, velY).toProto());
        byte[] buf = toSend.build().toByteArray();
        
        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, 
                InetAddress.getByName("localhost"), port);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
