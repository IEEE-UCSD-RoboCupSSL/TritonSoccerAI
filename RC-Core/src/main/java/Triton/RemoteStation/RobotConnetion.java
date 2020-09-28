package Triton.RemoteStation;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;

import Proto.MessagesRobocupSslGeometry.SSL_GeometryFieldSize;
import Proto.RemoteCommands.Remote_Geometry;
import Triton.DesignPattern.PubSubSystem.Subscriber;

public class RobotConnetion {
    private ExecutorService pool;

    private Socket tcpClientSocket;
    private DataOutputStream tcpOut;
    private BufferedReader tcpIn;

    private RobotUDPStream commandStream;
    private RobotUDPStream visionStream;
    private RobotUDPStream dataStream;

    private Subscriber<SSL_GeometryFieldSize> fieldSizeSub;

    private boolean isConnected;
    // private Publisher<> tcpConnectPub;

    public RobotConnetion(ExecutorService pool) {
        this.pool = pool;
        fieldSizeSub = new Subscriber<SSL_GeometryFieldSize>("geometry", "fieldSize", 1);
    }

    public boolean connect(String ip, int port) {
        try {
            tcpClientSocket = new Socket(ip, port);
            tcpOut = new DataOutputStream(tcpClientSocket.getOutputStream());
            tcpIn = new BufferedReader(new InputStreamReader(tcpClientSocket.getInputStream()));
            tcpOut.writeBytes("HELLO WORLD");
            isConnected = true;
            return true;
        } catch (UnknownHostException e) {
            isConnected = false;
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean sendGeometry() {
        while (!fieldSizeSub.subscribe());

        SSL_GeometryFieldSize fieldSize = fieldSizeSub.pollMsg();
        Remote_Geometry.Builder toSend = Remote_Geometry.newBuilder();
        toSend.setFieldLength(fieldSize.getFieldLength());
        toSend.setFieldWidth(fieldSize.getFieldWidth());
        toSend.setGoalDepth(fieldSize.getGoalDepth());
        toSend.setGoalWidth(fieldSize.getGoalWidth());

        byte[] geoByteArray = toSend.build().toByteArray();
        try {
            tcpOut.write(geoByteArray);
            String line = tcpIn.readLine();
            if (line.equals("GEOMETRY RECEIVED"))
                return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public void commandUDPBuilder(String ip, int port) {
        commandStream = new RobotUDPStream(ip, port);
    }

    public void visionStreamBuilder(String ip, int port) {
        visionStream = new RobotUDPStream(ip, port);
    }

    public void dataStreamBuilder(String ip, int port) {
        dataStream = new RobotUDPStream(ip, port);
    }
}
