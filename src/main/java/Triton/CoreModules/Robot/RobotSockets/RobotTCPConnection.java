package Triton.CoreModules.Robot.RobotSockets;

import Triton.Config.OldConfigs.ObjectConfig;
import Triton.Misc.ModulePubSubSystem.*;
import Triton.PeriphModules.Detection.RobotData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * TCP connection to robot
 */
public class RobotTCPConnection {
    private final String ip;
    private final int port;
    private final int ID;
    private final Publisher<Boolean> dribStatPub;
    private final Subscriber<RobotData> allySub;
    private final Publisher<String> tcpCommandPub;
    private final Subscriber<String> tcpCommandSub;
    private final Publisher<Boolean> tcpInitPub;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected;

    private SendTCPRunnable sendTCP;
    private ReceiveTCPRunnable receiveTCP;

    /**
     * Constructs TCP connection
     *
     * @param ip   ip of connection
     * @param port to connect to
     * @param ID   ID of robot
     */
    public RobotTCPConnection(String ip, int port, int ID) {
        this.ip = ip;
        this.port = port;
        this.ID = ID;

        dribStatPub = new FieldPublisher<>("Ally drib", "" + ID, false);
        allySub = new FieldSubscriber<>("detection", ObjectConfig.MY_TEAM.name() + ID);
        tcpCommandPub = new MQPublisher<>("tcpCommand", "" + ID);
        tcpCommandSub = new MQSubscriber<>("tcpCommand", "" + ID);
        tcpInitPub = new FieldPublisher<>("tcpInit", "" + ID, true);
    }

    /**
     * Begin connection
     *
     * @return true if connection was successfully established
     */
    public boolean connect() throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String line = in.readLine();
        System.out.printf("Ally %d TCP : %s\n", ID, line);
        if (line.equals("CONNECTION ESTABLISHED")) {
            isConnected = true;

            sendTCP = new SendTCPRunnable(out);
            receiveTCP = new ReceiveTCPRunnable(in, ID);
            return true;
        }
        return false;
    }

    /**
     * Sends initial location to robot
     */
    public void sendInit() {
        if (!isConnected) {
            return;
        }

        subscribe();
        RobotData allyData = allySub.getMsg();

        String str = String.format("init %d %d", (int) allyData.getPos().x, (int) allyData.getPos().y);
        tcpCommandPub.publish(str);
    }

    /**
     * Subscribe to publishers
     */
    private void subscribe() {
        try {
            allySub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns true if there is still a connection
     *
     * @return true if there is still a connection
     */
    public boolean isConnected() {
        return isConnected;
    }

    public SendTCPRunnable getSendTCP() {
        return sendTCP;
    }

    public ReceiveTCPRunnable getReceiveTCP() {
        return receiveTCP;
    }

    /**
     * Runnable to send TCP packets
     */
    private class SendTCPRunnable implements Runnable {
        private final PrintWriter out;

        public SendTCPRunnable(PrintWriter out) {
            this.out = out;
        }

        @Override
        public void run() {
            try {
                subscribe();

                while (true) { // delay added
                    String msg = tcpCommandSub.getMsg();
                    // System.out.println(msg);
                    out.println(msg);

                    Thread.sleep(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void subscribe() {
            try {
                tcpCommandSub.subscribe(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Runnable to receive TCP packets
     */
    private class ReceiveTCPRunnable implements Runnable {
        private final BufferedReader in;
        private final int ID;

        public ReceiveTCPRunnable(BufferedReader in, int ID) {
            this.in = in;
            this.ID = ID;
        }

        @Override
        public void run() {
            try {
                while (true) { // delay added
                    String line = in.readLine();
                    //System.out.printf("Ally %d TCP : %s\n", ID, line);

                    switch (line) {
                        case "BallOnHold" -> dribStatPub.publish(true);
                        case "BallOffHold" -> dribStatPub.publish(false);
                        case "Initialized" -> tcpInitPub.publish(true);
                    }

                    Thread.sleep(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
