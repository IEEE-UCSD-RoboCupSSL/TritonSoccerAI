package Client;

import java.io.*;
import java.net.*;
import Client.RemoteCommands.Remote_Geometry;

public class TCPServer {
    
    public static final int TCP_INIT_PORT = 8901;

    public static void main(String[] args) {
        if (args.length < 2) return;
 
        int robotID = Integer.parseInt(args[0]);
        int port    = Integer.parseInt(args[1]);

        try {
            int TCP_port = 8900 + robotID;
            ServerSocket serverSocket = new ServerSocket(TCP_port);

            System.out.println("Robot " + robotID + " is listening on port " + TCP_port);

            Socket socket = serverSocket.accept();
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            out.writeInt(robotID); // Write ID
            out.writeInt(port); // Write Port

            String msg = in.readLine(); // Read confirmation from client
            System.out.println(msg);

            int buf_size = in.readInt(); // Read geometry size
            byte[] buf = in.readNBytes(buf_size); // Read geometry
            Remote_Geometry geometry = Remote_Geometry.parseFrom(buf);
            System.out.println(geometry);

            while(true); // If wanted, can send a byte and receive server message
            
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
