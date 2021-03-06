package Triton.CoreModules.Robot.RobotSockets;

import Triton.Config.ConnectionConfig;
import Triton.CoreModules.Robot.Team;

/**
 * Contains all connections of a single robot
 */
public class RobotConnection {
    private final int ID;
    private String ip;
    private int port;

    private RobotTCPConnection tcpConnect;
    private RobotUDPStream udpStream;

    /**
     * Construct a RobotConnection for specified robot
     *
     * @param ID ID of the robot
     */
    public RobotConnection(int ID) {
        this.ID = ID;

        switch (ID) {
            case 0 -> {
                ip = ConnectionConfig.ROBOT_0_IP.getValue0();
                port = ConnectionConfig.ROBOT_0_IP.getValue1();
            }
            case 1 -> {
                ip = ConnectionConfig.ROBOT_1_IP.getValue0();
                port = ConnectionConfig.ROBOT_1_IP.getValue1();
            }
            case 2 -> {
                ip = ConnectionConfig.ROBOT_2_IP.getValue0();
                port = ConnectionConfig.ROBOT_2_IP.getValue1();
            }
            case 3 -> {
                ip = ConnectionConfig.ROBOT_3_IP.getValue0();
                port = ConnectionConfig.ROBOT_3_IP.getValue1();
            }
            case 4 -> {
                ip = ConnectionConfig.ROBOT_4_IP.getValue0();
                port = ConnectionConfig.ROBOT_4_IP.getValue1();
            }
            case 5 -> {
                ip = ConnectionConfig.ROBOT_5_IP.getValue0();
                port = ConnectionConfig.ROBOT_5_IP.getValue1();
            }
            default -> System.out.println("Invalid Robot ID");
        }
    }

    /**
     * Constructs a tcp connection
     */
    public void buildTcpConnection() {
        tcpConnect = new RobotTCPConnection(ip, port + ConnectionConfig.TCP_OFFSET, ID);
    }

    /**
     * Constructs the command UDP stream
     */
    public void buildUDPStream() {
        udpStream = new RobotUDPStream(ip, port + ConnectionConfig.UDP_OFFSET, ID);
    }

    /**
     * @return the tcp connection
     */
    public RobotTCPConnection getTCPConnection() {
        return tcpConnect;
    }

    /**
     * @return the command UDP stream
     */
    public RobotUDPStream getUDPStream() {
        return udpStream;
    }
}
