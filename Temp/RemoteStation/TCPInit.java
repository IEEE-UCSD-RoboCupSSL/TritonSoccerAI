package Triton.RemoteStation;

import Triton.Geometry.*;
import Triton.Shape.*;
import Triton.Config.ObjectConfig;
import Triton.Config.ConnectionConfig;

import java.io.*;
import java.net.*;

import java.util.HashMap;

import Proto.MessagesRobocupSslGeometry.SSL_GeometryFieldSize;
import Proto.RemoteCommands.Remote_Geometry;
import Triton.DesignPattern.PubSubSystem.Publisher;
import Triton.DesignPattern.PubSubSystem.Subscriber;
import Triton.DesignPattern.PubSubSystem.Module;

public class TCPInit {

    private static byte[] geometry;
    private static Subscriber<SSL_GeometryFieldSize> fieldSizeSub;

    private class TCPConnection implements Module {

        private int ID;

        public TCPConnection(int ID) {
            this.ID = ID;
            fieldSizeSub = new Subscriber<SSL_GeometryFieldSize>("geometry", "fieldSize");
        }

        public void run() {
            try {
                System.out.println("Connecting to Robot " + this.ID + " ...");
                Socket socket = new Socket("localhost", ConnectionConfig.TCP_PORTS[ID]);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());

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
        while (!fieldSizeSub.subscribe());

        SSL_GeometryFieldSize fieldSize = fieldSizeSub.pollMsg();
        toSend.setFieldLength(fieldSize.getFieldLength());
        toSend.setFieldWidth(fieldSize.getFieldWidth());
        toSend.setGoalDepth(fieldSize.getGoalDepth());
        toSend.setGoalWidth(fieldSize.getGoalWidth());
        geometry = toSend.build().toByteArray();
    }
}