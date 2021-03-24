package Triton.CoreModules.Robot.RobotSockets;

import Triton.Config.Config;
import Triton.Config.ConnectionProperties;

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

        try {
            ConnectionProperties conn = Config.load().getConnectionProperties();
            ip = conn.getRobotIp().get(ID).getIp();
            port = conn.getRobotIp().get(ID).getPort();
        } catch (Exception e) {
            System.out.println("Invalid Robot ID");
        }
    }

    /**
     * Constructs a tcp connection
     */
    public void buildTcpConnection() {
        tcpConnect = new RobotTCPConnection(ip,
                port + Config.load().getConnectionProperties().getTcpOffset(), ID);
    }

    /**
     * Constructs the command UDP stream
     */
    public void buildUDPStream() {
        udpStream = new RobotUDPStream(ip,
                port + Config.load().getConnectionProperties().getUdpOffset(), ID);
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
