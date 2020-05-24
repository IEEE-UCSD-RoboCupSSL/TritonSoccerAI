package Triton;
import Proto.*;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.time.ZonedDateTime; 

public class ConnectionToSim {
    
    private static ConnectionToSim single_instance = null; 

    private static InetAddress ipAddr;
    private static int port, id;
    private static DatagramSocket socket;
    private static byte[] buffer;
    private static float timeStamp = 0 , 
                         kickspeedx = 0, 
                         kickspeedz = 0, 
                         velx = 0, 
                         vely = 0, 
                         velz = 0;
    private static boolean spinner = false, 
                           wheelSpeed = false,
                           isYellow = false;
    private static long t_init;

    private static long getTimeMs() {
        return ZonedDateTime.now().toInstant().toEpochMilli() - t_init;
    } 

    public static ConnectionToSim getInstance() 
    { 
        if (single_instance == null) 
            single_instance = new ConnectionToSim(); 

        try {
            socket = new DatagramSocket();
        }
        catch (Exception e){ 
            System.out.println(e);
        }
  
        t_init = ZonedDateTime.now().toInstant().toEpochMilli();
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
    

    public static void send() {
        timeStamp = (float)getTimeMs();
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
            //System.out.println(printBuffer);
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

        
        ConnectionToSim.setVel(1.0f, 0.0f, 0.0f);
        long t0 = getTimeMs();
        while (getTimeMs() - t0 < 3000) {
            ConnectionToSim.send();
        }

        ConnectionToSim.setVel(0.0f, 0.0f, 0.0f);
        t0 = getTimeMs();
        while(getTimeMs() - t0 < 1000) {
            ConnectionToSim.send();
        }

        ConnectionToSim.setVel(0.0f, -1.0f, 0.0f);
        t0 = getTimeMs();
        while(getTimeMs() - t0 < 1000) {
            
            ConnectionToSim.send();
        }

        ConnectionToSim.setVel(0.0f, 0.0f, 0.0f);
        t0 = getTimeMs();
        while(getTimeMs() - t0 < 1000) { 
            ConnectionToSim.send();
        }

        ConnectionToSim.setVel(0.0f, 0.0f, 10.0f);
        t0 = getTimeMs();
        while(getTimeMs() - t0 < 1000) { 
            ConnectionToSim.send();
        }


        ConnectionToSim.setVel(0.0f, 0.0f, 0.0f);
        t0 = getTimeMs();
        while(getTimeMs() - t0 < 1000) { 
            ConnectionToSim.send();
        }
    }
    
}