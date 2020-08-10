package Triton.RemoteStation;

import java.io.*;
import java.net.*;
import java.util.concurrent.Callable;
import java.util.HashMap;

public class TCPInit implements Callable<HashMap<Integer, Integer>> {

    public static HashMap<Integer, Integer> ports; // Team: is YELLOW

    public static final int ROBOT_COUNT = 6;
    public static final int TCP_INIT_PORT = 8901;

    public TCPInit() {
        ports = new HashMap<Integer, Integer>();

        //ports.put(false, new HashMap<Integer, Integer>());
        //ports.put(true,  new HashMap<Integer, Integer>());
    }

    public HashMap<Integer, Integer> call() {

        try (ServerSocket serverSocket = new ServerSocket(TCP_INIT_PORT)) {
            System.out.println("Server is listening on port " + TCP_INIT_PORT);
 
            while (ports.size() != ROBOT_COUNT) {
                Socket socket = serverSocket.accept();

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

                /* Parsing incoming robot connection info */
                String line = in.readLine();
                String[] splitLine = line.split(",");
                
                int ID = Integer.parseInt(splitLine[0]);
                int port = Integer.parseInt(splitLine[1]);

                ports.put(ID, port);

                System.out.printf("Robot %d listening to UDP commands on port: %d\n", 
                    ID, port);
                out.printf("Server will be sending UDP commands to robot %d on port: %d\n", 
                    ID, port);
                out.printf("GEOMETRY PROTOBUF\n");

                out.close();
                in.close();
                socket.close();
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }

        return ports;
    }
}