package SimuBot.Control.VirtualBot;



import java.net.DatagramSocket;
import java.lang.System.Logger.Level;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.*;
import Protobuf.*;
import Protobuf.RemoteCommands.data_request;
import Protobuf.RemoteCommands.remote_commands;

public class VirtualBot {
    private static DatagramSocket socket;
    private static String ip;
    private static int port;
    public static boolean isConnectedToGrSim = false;
    private int botID;
    private String teamColor;
    
    private RemoteCommands.remote_commands cmds;
    private RemoteCommands.static_data sdata;



    public VirtualBot() {
        // create new thread
    }

    public static void setIPAddrPort(String ipAddr, int port) {
        VirtualBot.ip = ipAddr;
        VirtualBot.port = port;
    }

    public void setBotID_Color(int botID, String teamColor) {
        this.botID = botID;
        this.teamColor = teamColor;
    }

    public static void connectToGrSim() {
        try {
            socket = new DatagramSocket();
            isConnectedToGrSim = true;
        }
        catch (Exception e){ 
            isConnectedToGrSim = false;
            System.out.println(e);
        }
    }
    public static void disconnectToGrSim() {
        socket = null;
    }

    public void setCommands(RemoteCommands.remote_commands cmds) {
        this.cmds = cmds;
    }

    public RemoteCommands.data_request getDataRequested() {
        // To-do
        return cmds.getRequest();
    }


    public void setStaticData(RemoteCommands.static_data sdata) {
        this.sdata = sdata;
    }
}