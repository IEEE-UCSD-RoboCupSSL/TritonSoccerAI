package Triton;
import Proto.*;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class ConnectionToSim {
    
    private static ConnectionToSim single_instance = null; 

    private static InetAddress ipAddr;
    private static int port, id;
    private static DatagramSocket socket;
    private static byte[] buffer;
    private static float timeStamp, kickspeedx, kickspeedz, velx, vely, velz;
    private static boolean spinner, wheelSpeed, isYellow;
    
    public static ConnectionToSim getInstance() 
    { 
        if (single_instance == null) 
            single_instance = new ConnectionToSim(); 
  
        return single_instance; 
    }

    public static void setIp(String ip) {
        try {
            ipAddr = InetAddress.getByName(ip);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void setPort(int _port) {
        port = _port;
    }

    public static void setId(int robotId) {
        id = robotId;
    }

    public static void setIsYellow(boolean isYellowTeam) {
        isYellow = isYellowTeam;
    }

    public static void setVel(float x, float y, float z) {
        velx = x;
        vely = y;
        velz = z;
    }
    
    public static void connectToGrSim() {
        try {
            socket = new DatagramSocket();
        }
        catch (Exception e){ 
            System.out.println(e);
        }
    }
    public static void disconnectToGrSim() {
        socket = null;
    }

    public static void send() {
        GrSimCommands.grSim_Robot_Command robotCommand = GrSimCommands.grSim_Robot_Command.newBuilder().setId(id)
                .setKickspeedx(kickspeedx).setKickspeedz(kickspeedz).setVeltangent(velx)
                .setVelnormal(vely).setVelangular(velz).setSpinner(spinner)
                .setWheelsspeed(wheelSpeed).build();
        GrSimCommands.grSim_Commands grSimCommand = GrSimCommands.grSim_Commands.newBuilder().setTimestamp(timeStamp)
                .setIsteamyellow(isYellow).addRobotCommands(robotCommand).build();
        GrSimPacket.grSim_Packet packet = GrSimPacket.grSim_Packet.newBuilder().setCommands(grSimCommand).build();

        String printBuffer = packet.toString();
        buffer = packet.toByteArray();
        try {
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length, ipAddr, port);
            socket.send(dp);
            System.out.println(printBuffer);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public static void test() {
        ConnectionToSim.getInstance();
        
        
        ConnectionToSim.setIsYellow(false);
        ConnectionToSim.setId(1);
        ConnectionToSim.setIp("127.0.0.1");
        ConnectionToSim.setPort(20011);
        ConnectionToSim.connectToGrSim();

        ConnectionToSim.setVel(10, 0, 0);
        
        while (true) {
            ConnectionToSim.send();
        }
    }
    
}