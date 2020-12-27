package Triton.Modules.RemoteStation;

import Triton.Config.ObjectConfig;
import Triton.Dependencies.DesignPattern.PubSubSystem.FieldSubscriber;
import Triton.Dependencies.DesignPattern.PubSubSystem.Module;
import Triton.Dependencies.DesignPattern.PubSubSystem.Subscriber;
import Triton.Modules.Detection.RobotData;
import Triton.Modules.Detection.Team;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * TCP connection to robot
 */
public class RobotTCPConnection implements Module {
    private final String ip;
    private final int port;
    private final int ID;

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private final Subscriber<RobotData> robotDataSub;
    private Subscriber<String> tcpCommandSub;
    private boolean isConnected;

    /**
     * Constructs TCP connection
     * @param ip ip of connection
     * @param port to connect to
     * @param ID ID of robot
     */
    public RobotTCPConnection(String ip, int port, int ID) {
        this.ip = ip;
        this.port = port;
        this.ID = ID;

        String name = (ObjectConfig.MY_TEAM == Team.YELLOW) ? "yellow robot data" + ID : "blue robot data" + ID;
        robotDataSub = new FieldSubscriber<>("detection", name);
        //tcpCommandSub = new MQSubscriber<String>("tcpCommand", name);
    }

    /**
     * Begin connection
     * @return true if connection was successfully established
     */
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

    /**
     * Sends initial location to robot
     */
    public void sendInit() {
        subscribe();
        RobotData data = robotDataSub.getMsg();

        String str = String.format("init %d %d", (int) -data.getPos().y, (int) data.getPos().x);
        out.println(str);

        try {
            String line = in.readLine();
            System.out.println(ID + " " + line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
    }

    /**
     * Subscribe to publishers
     */
    private void subscribe() {
        try {
            robotDataSub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the status of the dribbler
     * @return true if
     */
    public boolean requestDribblerStatus() {
        out.println("");
        try {
            return Boolean.parseBoolean(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns true if there is still a connection
     * @return true if there is still a connection
     */
    public boolean isConnected() {
        return isConnected;
    }
}
