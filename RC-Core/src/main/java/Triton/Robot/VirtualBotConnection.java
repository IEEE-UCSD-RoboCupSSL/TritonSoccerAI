package Triton.Robot;

import Proto.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.*;


// connection to a virtual bot in grSim simulator
public class VirtualBotConnection implements RobotConnection{
    
    final static int MAX_BUFFER_SIZE       = 65536;
    final static String CONNECT_REQUEST    = "request-connection:";
    final static String CONNECT_CONFIRM    = "connected";
    final static String DISCONNECT_REQUEST = "request-disconnect";
    final static String DISCONNECT_CONFIRM = "disconnected";

    public static Logger logger = Logger.getLogger(VirtualBotConnection.class.getName());
    public int botID;
    public String teamColor; 
    private InetAddress ip; 
    private int port;
    private DatagramSocket socket = null;
    private byte[] socketBuffer;

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
        "[%4$-7s] %5$s %n");
    }

    public VirtualBotConnection(String targerIpAddr, int targetPort, String teamColor, int botID) {
        
        try {
            this.socket = new DatagramSocket();
            this.ip = InetAddress.getByName(targerIpAddr);
            this.port = targetPort;
        }
        catch (Exception e){
            logger.log(Level.SEVERE, e.toString());
        }
        this.botID = botID;
        this.teamColor = teamColor;
    }

    
    public void connect() {
        String received = null;
        do {    
            socketBuffer = new byte[MAX_BUFFER_SIZE];
            socketBuffer = (CONNECT_REQUEST + " " + teamColor + " " + botID).getBytes();
            try {
                socket.send(new DatagramPacket(socketBuffer, socketBuffer.length, ip, port));
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.toString());
            }
        
            socketBuffer = new byte[MAX_BUFFER_SIZE];
            DatagramPacket response = new DatagramPacket(socketBuffer, socketBuffer.length);
            try {
                logger.log(Level.INFO, "Connecting to " + teamColor + " " + botID + " ......");
                socket.receive(response);
                received = new String(response.getData(), 0, response.getLength());
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, e.toString());
            }  
            if(!(received.trim()).equals(CONNECT_CONFIRM)) {
                logger.log(Level.SEVERE, "Unable to connect to Robot: " + teamColor + " " + botID 
                    + " ; " + "String received: " + received);
            }
            // System.out.println(received.trim() + " | " + CONNECT_CONFIRM);
        } while (!(received.trim()).equals(CONNECT_CONFIRM));
        logger.log(Level.INFO, "Successfully connected to " + teamColor + " " + botID);
    }

    public void disconnect() {
        String received = null;
        do {
            socketBuffer = new byte[MAX_BUFFER_SIZE];
            socketBuffer = DISCONNECT_REQUEST.getBytes();
            try {
                socket.send(new DatagramPacket(socketBuffer, socketBuffer.length, ip, port));
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.toString());
            }
        
            socketBuffer = new byte[MAX_BUFFER_SIZE];
            DatagramPacket response = new DatagramPacket(socketBuffer, socketBuffer.length);
            try {
                logger.log(Level.INFO, "Disconnecting " + teamColor + " " + botID + " ......");
                socket.receive(response);
                received = new String(response.getData(), 0, response.getLength());
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, e.toString());
            }  
        } while(!(received.trim()).equals(DISCONNECT_CONFIRM));
        logger.log(Level.INFO, "Disconnected to " + teamColor + " " + botID);
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
        
        VirtualBotConnection[] botConns = { new VirtualBotConnection("127.0.0.1", 8881, "Blue", 1), 
                                            new VirtualBotConnection("127.0.0.1", 8882, "Blue", 2),
                                            new VirtualBotConnection("127.0.0.1", 8883, "Blue", 3),
                                            new VirtualBotConnection("127.0.0.1", 8884, "Blue", 4),
                                            new VirtualBotConnection("127.0.0.1", 8885, "Blue", 5),
                                            new VirtualBotConnection("127.0.0.1", 8886, "Blue", 6)
                                            };

        // shell cmd to test: nc -lvu [ip] [port]
        // e.g. "nc -lvu 127.0.0.1 8881"
        for(VirtualBotConnection botConn : botConns) {
            botConn.connect();
        }

        /*
        for(VirtualBotConnection botConn : botConns) {
            botConn.disconnect();
        }
        */
    }
}