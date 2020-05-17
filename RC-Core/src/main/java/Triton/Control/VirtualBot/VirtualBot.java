package Triton.Control.VirtualBot;



import java.net.DatagramSocket;
import java.lang.System.Logger.Level;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.*;
import Proto.*;
import Proto.RemoteCommands.data_request;
import Proto.RemoteCommands.remote_commands;


public class VirtualBot {


    private static DatagramSocket socket;
    private static String ip;
    private static int port;
    public static boolean isConnectedToGrSim = false;
    private int botID;
    private String teamColor;
    
    private RemoteCommands.remote_commands cmds;
    


    // Internal data/cmd representation





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

    public void receivePacketFromRemote(RemoteCommands.remote_commands cmds) {
        this.cmds = cmds;
        if(cmds.getCtrl() != null) {
            updateControl(cmds.getCtrl());
        }
        if(cmds.getCdata() != null) {
            updateCloudData(cmds.getCdata());
        }
        if(cmds.getCustCtrl() != null) {
            updateCustomCtrl(cmds.getCustCtrl());
        }
        if(cmds.getTask() != null) {
            updateTask(cmds.getTask());
        }

    }

    public RemoteCommands.data_request sendDataToRemote() {
        // for future extension, currently useless 
        return cmds.getRequest();
    }

    void updateControl(RemoteCommands.control ctrl) {
        System.out.println(ctrl.toString());
    }

    void updateCloudData(RemoteCommands.data_send cData) {
        // To-do
    }

    void updateCustomCtrl(RemoteCommands.custom_control cusCtrl) {
        // To-do
    }

    void updateTask(RemoteCommands.custom_task task) {
        // To-do
    }

}