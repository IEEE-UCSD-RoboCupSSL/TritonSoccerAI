package Triton;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.*;
import java.util.*;


public class RemoteListener {
    
    final static int    MAX_BUFFER_SIZE    = 65536;
    final static int    MAX_NUM_ROBOTS     = 6;
    final static String CONNECT_REQUEST    = "request-connection:";
    final static String CONNECT_CONFIRM    = "connected";
    final static String DISCONNECT_REQUEST = "request-disconnect";
    final static String DISCONNECT_CONFIRM = "disconnected";

    public static Logger logger = Logger.getLogger(RemoteListener.class.getName());
    
    private static List<Integer> blueRegisteredBots = new ArrayList<Integer>();
    private static List<Integer> yellowRegisteredBots = new ArrayList<Integer>();

    private DatagramSocket socket;
    private byte[] socketBuffer;

    private int port;
    private InetAddress ip;
    private boolean available;

    private String teamColor;
    private int botID;
    
    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
        "[%4$-7s] %5$s %n");
    }

    public RemoteListener(int portToListen) {
        this("127.0.0.1", portToListen);
    }

    public RemoteListener(String ipAddr, int portToListen) {
        try {
            this.ip = InetAddress.getByName(ipAddr);
            this.port = portToListen;
            socket = new DatagramSocket(portToListen);
        } 
        catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
        }
        available = true;
    }

    public void waitForConnectRequest() throws IOException {
        DatagramPacket receivedPacket = null;
        DatagramPacket responsePacket;
        StringTokenizer tokenizer;
        List<String> tokens;
        String color;
        int id;

        logger.log(Level.INFO, "Waiting for connection request on port " + port);
        
        while(available) {
            socketBuffer = new byte[MAX_BUFFER_SIZE];
            receivedPacket = new DatagramPacket(socketBuffer, socketBuffer.length);
            socket.receive(receivedPacket);

            String received = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
            logger.log(Level.INFO, "Received request message: " + received);
            
            tokenizer = new StringTokenizer(received, " ");
            tokens = new ArrayList<String>();
            while(tokenizer.hasMoreElements()) {
                tokens.add(tokenizer.nextToken());
            }

            if(tokens.size() == 3) {
                
                if(tokens.get(0).trim().equals(CONNECT_REQUEST)) {
                    color = tokens.get(1);
                    id = Integer.parseInt(tokens.get(2));
                    if(color.toLowerCase().trim().equals("blue")) {
                        if(!blueRegisteredBots.contains(id)) {
                            teamColor = color;
                            botID = id;
                            blueRegisteredBots.add(id);
                            available = false;
                        }
                    }
                    if(color.toLowerCase().trim().equals("yellow")) {
                        if(!yellowRegisteredBots.contains(id)) {
                            teamColor = color;
                            botID = id;
                            blueRegisteredBots.add(id);
                            available = false;
                        }
                    }
                }
            }
            if(available) {
                socketBuffer = new byte[MAX_BUFFER_SIZE];
                socketBuffer = "connection failed".getBytes();
                responsePacket = new DatagramPacket(socketBuffer, socketBuffer.length,
                                receivedPacket.getAddress(),
                                receivedPacket.getPort());
                socket.send(responsePacket);
            }

        }

        logger.log(Level.INFO, "This instance is registered as Team [" + teamColor 
        + "] ID[" + botID + "]");

        // reply confirmation
        socketBuffer = new byte[MAX_BUFFER_SIZE];
        socketBuffer = CONNECT_CONFIRM.getBytes();
        responsePacket = new DatagramPacket(socketBuffer, socketBuffer.length,
                                receivedPacket.getAddress(),
                                receivedPacket.getPort());
        socket.send(responsePacket);
    }



    public static void test() {
        RemoteListener[] listeners = { new RemoteListener(8881),
                                       new RemoteListener(8882),
                                       new RemoteListener(8883),
                                       new RemoteListener(8884),
                                       new RemoteListener(8885),
                                       new RemoteListener(8886), };
        for(RemoteListener listener : listeners) {
            try {
                listener.waitForConnectRequest();
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.toString());
            }
        }
    }

}