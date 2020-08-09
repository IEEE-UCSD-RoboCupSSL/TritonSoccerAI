package Client;

import java.io.*;
import java.net.*;

/**
 * This program demonstrates a simple TCP/IP socket client.
 *
 * @author www.codejava.net
 */

public class TCPClient {
    
    public static final int TCP_INIT_PORT = 8901;
 
    public static void main(String[] args) {
        if (args.length < 3) return;
 
        String team    = args[0];
        String robotID = args[1];
        String port    = args[2];
 
        try (Socket socket = new Socket("localhost", TCP_INIT_PORT)) {
            
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

            System.out.println("Connecting to server...");

            /*** TODO: Need to be implemented in CPP */
            out.printf("%s,%s,%s\n", team, robotID, port);
            System.out.println(in.readLine()); // Receiving server binding message
            System.out.println(in.readLine()); // Receiving geometry protobuf
            /*** TODO: Need to be implemented in CPP */

            out.close();
            in.close();
            socket.close();
 
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
