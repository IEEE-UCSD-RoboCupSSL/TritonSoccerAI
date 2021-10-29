package triton.coreModules.robot.robotSockets;

import triton.config.Config;
import triton.config.ConnectionConfig;
import triton.coreModules.robot.Team;

/**
 * Contains all connections of a single robot
 */
public class RobotConnection {
    private final int id;
    private String ip = null;
    private int tcpPort;
    private int udpPort;
    private Team myTeam;

    private RobotTCPConnection tcpConnect;
    private RobotUDPStream udpStream;

    /**
     * Construct a RobotConnection for specified robot
     *
     * @param id ID of the robot
     */
    public RobotConnection(Config config, int id) throws RuntimeException {
        this.id = id;
        this.myTeam = config.myTeam;

        for(ConnectionConfig.BotConn conn : config.connConfig.botConns) {
            if(conn.id == id) {
                ip = conn.ipAddr;
                tcpPort = conn.tritonBotTcpPort;
                udpPort = conn.tritonBotUdpPort;
            }
        }
        if(ip == null) {
            throw new RuntimeException("can't find a IP config with matching bot id");
        }
    }

    /**
     * Constructs a tcp connection
     */
    public void buildTcpConnection() {
        tcpConnect = new RobotTCPConnection(ip,tcpPort, id, myTeam);
    }

    /**
     * Constructs the command UDP stream
     */
    public void buildUDPStream() {
        udpStream = new RobotUDPStream(ip,udpPort, id, myTeam);
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
