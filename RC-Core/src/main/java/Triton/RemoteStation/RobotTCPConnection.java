package Triton.RemoteStation;

import java.net.*;
import java.util.concurrent.TimeoutException;

import Triton.DesignPattern.PubSubSystem.Module;
import Triton.Detection.RobotData;
import Triton.Detection.Team;
import Triton.Config.ObjectConfig;
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

        String name = (ObjectConfig.MY_TEAM == Team.YELLOW) ? "yellow robot data" + ID : "blue robot data" + ID;
        robotDataSub = new FieldSubscriber<RobotData>("detection", name);

        try {
            robotDataSub.subscribe(1000);
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

    public void sendInit() {
        RobotData data = robotDataSub.getMsg();

        out.format("init %f %f\n", data.getPos().x, data.getPos().y);
    }

    public boolean requestDribblerStatus() {
        out.format("\n");
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
    }
}
