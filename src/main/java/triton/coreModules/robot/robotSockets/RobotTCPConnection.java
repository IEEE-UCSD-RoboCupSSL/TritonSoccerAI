package triton.coreModules.robot.robotSockets;

import triton.coreModules.robot.Team;
import triton.misc.modulePubSubSystem.*;
import triton.periphModules.detection.RobotData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static triton.Util.delay;

/**
 * TCP connection to robot
 */
public class RobotTCPConnection {
    private final String ip;
    private final int port;
    private final int id;
    private final Publisher<Boolean> isDribbledPub;
    private final Subscriber<RobotData> allySub;
    private final Publisher<String> tcpCommandPub;
    private final Subscriber<String> tcpCommandSub;
    private final Publisher<Boolean> tcpInitPub;
    private Socket clientSocket;
    private PrintWriter socketOut;
    private BufferedReader socketIn;
    private boolean isConnected;

    private SendTCPRunnable sendTCP;
    private ReceiveTCPRunnable receiveTCP;

    /**
     * Constructs TCP connection
     *
     * @param ip   ip of connection
     * @param port to connect to
     * @param id   ID of robot
     */
    public RobotTCPConnection(String ip, int port, int id, Team myTeam) {
        this.ip = ip;
        this.port = port;
        this.id = id;

        isDribbledPub = new FieldPublisher<>("From:RobotTCPConnection", "Drib " + id, false);
        allySub = new FieldSubscriber<>("From:DetectionModule", myTeam.name() + id);
        tcpCommandPub = new MQPublisher<>("From:RobotTCPConnection", "TcpCommand " + id);
        tcpCommandSub = new MQSubscriber<>("From:RobotTCPConnection", "TcpCommand " + id);
        tcpInitPub = new FieldPublisher<>("From:RobotTCPConnection", "TcpInit " + id, true);
    }

    /**
     * Begin connection
     *
     * @return true if connection was successfully established
     */
    public boolean connect() throws IOException {
        boolean isTcpConnected = false;
        do {
            try {
                clientSocket = new Socket(ip, port);
                isTcpConnected = true;
            } catch (IOException e) {
                System.out.println("Failed at connecting TritonBot(cpp)'s tcp port, will retry connection");
                delay(1000);
            }
        } while(!isTcpConnected);


        socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
        socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String line = socketIn.readLine();
        System.out.printf("\033[0;32mAlly %d TCP : %s\033[0m\n", id, line);
        if (line.equals("CONNECTION ESTABLISHED")) {
            isConnected = true;

            sendTCP = new SendTCPRunnable(socketOut);
            receiveTCP = new ReceiveTCPRunnable(socketIn, id);
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
        private final PrintWriter socketOut;

        public SendTCPRunnable(PrintWriter socketOut) {
            this.socketOut = socketOut;
        }

        @Override
        public void run() {
            try {
                subscribe();

                while (true) { // delay added
                    String msg = tcpCommandSub.getMsg();
                    // System.out.println(msg);
                    socketOut.println(msg);

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
        private final BufferedReader socketIn;
        private final int ID;

        public ReceiveTCPRunnable(BufferedReader socketIn, int ID) {
            this.socketIn = socketIn;
            this.ID = ID;
        }

        @Override
        public void run() {
            try {
                while (true) { // delay added
                    String line = socketIn.readLine();
                    //System.out.printf("Ally %d TCP : %s\n", ID, line);

                    switch (line) {
                        case "BallOnHold" -> isDribbledPub.publish(true);
                        case "BallOffHold" -> isDribbledPub.publish(false);
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
