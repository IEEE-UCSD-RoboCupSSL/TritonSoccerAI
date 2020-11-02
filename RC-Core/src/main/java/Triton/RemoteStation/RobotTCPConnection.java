package Triton.RemoteStation;

import java.net.*;

import Triton.DesignPattern.PubSubSystem.Module;
import Triton.Detection.RobotData;
import Triton.DesignPattern.PubSubSystem.*;

import java.io.*;

public class RobotTCPConnection implements Module {
    private String ip;
    private int port;
    private int ID;

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private Subscriber<RobotData> robotDataSub;
    private boolean isConnected;

    public RobotTCPConnection(String ip, int port, int ID) {
        this.ip = ip;
        this.port = port;
        this.ID = ID;
        robotDataSub = new FieldSubscriber<RobotData>("detection", "" + ID);
    }

    public boolean connect() {
        try {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String line = in.readLine();
            if (line.equals("CONNECTION ESTABLISHED")) {
                isConnected = true;
                System.out.println(ID + " " + line);
                return true;
            }
            return false;
        } catch (UnknownHostException e) {
            isConnected = false;
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendInit(double x, double y) {
        out.format("init %f %f", x, y);
    }

    public boolean requestDribblerStatus() {
        out.format("");
        try {
            return Boolean.parseBoolean(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void run() {
        connect();
    }
}
