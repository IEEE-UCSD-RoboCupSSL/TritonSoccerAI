package Triton.Robot;

import Proto.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.*;
import java.util.Arrays;


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

    
    public void connect() throws IOException {
        String received = null;
        do {
            // Send connection info (team + ID) to the listener
            socketBuffer = new byte[MAX_BUFFER_SIZE];
            socketBuffer = (CONNECT_REQUEST + " " + teamColor + " " + botID).getBytes();
            socket.send(new DatagramPacket(socketBuffer, socketBuffer.length, ip, port));
        
            // Receive reply message from the listener
            socketBuffer = new byte[MAX_BUFFER_SIZE];
            DatagramPacket response = new DatagramPacket(socketBuffer, socketBuffer.length);
            logger.log(Level.INFO, "Connecting to " + teamColor + " " + botID + " ......");
            socket.receive(response);
            received = new String(response.getData(), 0, response.getLength());

            // Report error if reply message not received
            if(!(received.trim()).equals(CONNECT_CONFIRM)) {
                logger.log(Level.SEVERE, "Unable to connect to Robot: " + teamColor + " " + botID 
                    + " ; " + "String received: " + received);
            }
        } while (!(received.trim()).equals(CONNECT_CONFIRM));
        logger.log(Level.INFO, "Successfully connected to " + teamColor + " " + botID);
    }

    public void disconnect() throws IOException {
        String received = null;
        do {
            // Send disconnection info to the listener
            socketBuffer = new byte[MAX_BUFFER_SIZE];
            socketBuffer = DISCONNECT_REQUEST.getBytes();
            socket.send(new DatagramPacket(socketBuffer, socketBuffer.length, ip, port));
        
            // Receive reply message from the listener
            socketBuffer = new byte[MAX_BUFFER_SIZE];
            DatagramPacket response = new DatagramPacket(socketBuffer, socketBuffer.length);
            logger.log(Level.INFO, "Disconnecting " + teamColor + " " + botID + " ......");
            socket.receive(response);
            received = new String(response.getData(), 0, response.getLength());
        } while(!(received.trim()).equals(DISCONNECT_CONFIRM));
        
        logger.log(Level.INFO, "Disconnected to " + teamColor + " " + botID);
    }

    public void sendCommands(RemoteCommands.Remote_Commands cmds) throws IOException {
        socketBuffer = cmds.toByteArray();
        DatagramPacket dp = new DatagramPacket(socketBuffer, socketBuffer.length, ip, port);
        socket.send(dp);
    }
    
    public void initialize(RemoteCommands.Static_Data staticData) {
        RemoteCommands.Remote_Commands cmds = RemoteCommands.Remote_Commands
            .newBuilder().setToInit(staticData).build();
        try {
            sendCommands(cmds);
        } catch(IOException e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }

    public RemoteCommands.Data_Request receiveDataRequested(String dataName) throws IOException {
        
        RemoteCommands.Data_Request dr = RemoteCommands.Data_Request
            .newBuilder().setName(dataName).build();
        
        RemoteCommands.Remote_Commands cmds = RemoteCommands.Remote_Commands
            .newBuilder().setRequest(dr).build();
        
        sendCommands(cmds);

        socketBuffer = new byte[MAX_BUFFER_SIZE];
        DatagramPacket receivedPacket = new DatagramPacket(socketBuffer, socketBuffer.length);
        socket.receive(receivedPacket);

        RemoteCommands.Data_Request data 
            = RemoteCommands.Data_Request.parseFrom(trim(receivedPacket.getData()));
        
        

        return data;
    }

    public static byte[] trim(byte[] bytes) {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0) {
            --i;
        }
        return Arrays.copyOf(bytes, i + 1);
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
            try {
                botConn.connect();
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, e.toString());
            }
        }

        /*
        for(VirtualBotConnection botConn : botConns) {
            botConn.disconnect();
        }
        */
    }
}