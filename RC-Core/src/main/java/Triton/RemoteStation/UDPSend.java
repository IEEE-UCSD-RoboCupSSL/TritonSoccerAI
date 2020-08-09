package Triton.RemoteStation;

import java.util.Scanner;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

public class UDPSend implements Runnable {

    public HashMap<Boolean, HashMap<Integer, Integer>> ports; // Team: is YELLOW
    private DatagramSocket socket;

    public UDPSend(HashMap<Boolean, HashMap<Integer, Integer>> ports) {
        this.ports = ports;
        try {
            socket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Scanner s = new Scanner(System.in);
        while (true) {
            System.out.println("Enter a MoveTo Command: (format: TEAM ID CURR_LOC DEST_LOC DEST_VEL)");
            boolean team = s.next().equals("YELLOW") ? true : false;
            int id = s.nextInt();
            double posX = s.nextDouble();
            double posY = s.nextDouble();
            double desX = s.nextDouble();
            double desY = s.nextDouble();
            double velX = s.nextDouble();
            double velY = s.nextDouble();
            send(team, id, posX, posY, desX, desY, velX, velY);
        }
    }

    public void send(boolean team, int id, double posX, double posY, 
                     double desX, double desY, double velX, double velY) {
        int port = ports.get(team).get(id);

        String msg = String.format("%s %d %f %f %f %f %f %f", team ? "YELLOW" : "BLUE", 
            id, posX, posY, desX, desY, velX, velY);
             
        byte[] buf = msg.getBytes();
        
        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, 
                InetAddress.getByName("localhost"), port);

            socket.send(packet);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
