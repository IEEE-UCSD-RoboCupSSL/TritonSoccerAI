package Triton.RemoteStation;

import Triton.Geometry.*;
import Triton.Shape.*;
import Triton.Config.ObjectConfig;
import Triton.Config.ConnectionConfig;

import java.io.*;
import java.net.*;

import java.util.HashMap;
import Proto.RemoteCommands.Remote_Geometry;

public class TCPInit {
    
    private static byte[] geometry;
    private static StationData data = new StationData();

    private static class TCPConnection implements Runnable {
        
        private int ID;

        public TCPConnection(int ID) {
            this.ID = ID;
        }

        public void run() {
            try {
                System.out.println("Connecting to Robot " + this.ID + " ...");
                Socket socket = new Socket("localhost", ConnectionConfig.TCP_PORTS[ID]);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());

                int ID = in.readInt();
                int port = in.readInt();
                data.putPort(ID, port);

                System.out.println("Robot " + ID + " listening to UDP commands on TCP port: " + port);
                out.writeChars("Station will be sending UDP commands to robot " + ID + " on port: " + port + "\n");
                out.writeInt(geometry.length);
                out.write(geometry);
                
                /* Retain active connection */
                while (true) {
                    in.read();
                    out.writeChars("SERVER IS ACTIVE\n");
                }
            } catch (UnknownHostException ex) {
                System.out.println("Server not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O error: " + ex.getMessage());
            }
        }
    }

    public static void init() {
        Remote_Geometry.Builder toSend = Remote_Geometry.newBuilder();
        while (true) {
            try {
                Field field = GeometryData.get().getField();
                for (HashMap.Entry<String, Line2D> entry : field.lineSegments.entrySet()) {
                    toSend.putLines(entry.getKey(), entry.getValue().toProto());
                }
                toSend.setCenterCircleRadius(field.arcList.get(0).getRadius());
                break;
            } catch (Exception e) {
                // Geometry not ready, do nothing
            }
        }
        geometry = toSend.build().toByteArray();

        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            new Thread(new TCPConnection(i)).start();
        }

        while(data.getNumRobot() != ObjectConfig.ROBOT_COUNT);
        data.publish();
    }
}