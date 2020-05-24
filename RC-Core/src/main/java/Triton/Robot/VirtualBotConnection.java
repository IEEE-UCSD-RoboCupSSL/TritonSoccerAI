package Triton.Robot;

import Proto.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.*;


// connection to a virtual bot in grSim simulator
public class VirtualBotConnection implements RobotConnection{
    
    final static int MAX_BUFFER_SIZE = 10000000;

    public static Logger logger = Logger.getLogger(VirtualBotConnection.class.getName());
    public int botID;
    public String teamColor; 
    private static InetAddress ip; 
    private static int port = 8888;
    private static DatagramSocket socket = null;
    private byte[] socketBuffer = new byte[MAX_BUFFER_SIZE];

    public static void setIP(String ipAddr) {
        try {
            ip = InetAddress.getByName(ipAddr);
        }
        catch (Exception e){
            logger.log(Level.SEVERE, e.toString());
        }
    }
    
    public static void setPort(int port) {
        VirtualBotConnection.port = port;
    }

    public VirtualBotConnection(int botID, String teamColor) {
        
        
        if(socket == null) {
            try {
                socket = new DatagramSocket();
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, e.toString());
            }
        }
        this.botID = botID;
        this.teamColor = teamColor;
    }

    
    public void connect() {
        
    }

    public void disconnect() {
        
    }
    
    public void initialize(RemoteCommands.static_data staticData) {
        RemoteCommands.remote_commands cmds = RemoteCommands.remote_commands
            .newBuilder().setToInit(staticData).build();
        sendCommands(cmds);
    }


    public void sendCommands(RemoteCommands.remote_commands cmds) {
        // receiver from vBot's perspective; sender from this object's perspective
         
    }

    public RemoteCommands.data_request receiveDataRequested(String dataName) {
        
        RemoteCommands.data_request dr = RemoteCommands.data_request
            .newBuilder().setName(dataName).build();
        
        RemoteCommands.remote_commands cmds = RemoteCommands.remote_commands
            .newBuilder().setRequest(dr).build();
        
        sendCommands(cmds);

        // sender from vBot's perspective; receiver from this object's perspective
        return null;
    }


    public static void test() {

    }
}