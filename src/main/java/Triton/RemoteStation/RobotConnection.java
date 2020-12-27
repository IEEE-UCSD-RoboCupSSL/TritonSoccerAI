package Triton.RemoteStation;

/**
 * Contains all connections of a single robot
 */
public class RobotConnection {
    private final int ID;

    private RobotTCPConnection tcpConnect;
    private RobotCommandUDPStream commandStream;
    private RobotVisionUDPStream visionStream;
    private RobotDataUDPStream dataStream;

    /**
     * Construct a RobotConnection for specified robot
     *
     * @param ID ID of the robot
     */
    public RobotConnection(int ID) {
        this.ID = ID;
    }

    /**
     * Constructs a tcp connection
     *
     * @param ip   ip of connection
     * @param port port of connection
     */
    public void buildTcpConnection(String ip, int port) {
        tcpConnect = new RobotTCPConnection(ip, port, ID);
    }

    /**
     * Constructs the command UDP stream
     *
     * @param ip   ip to send to
     * @param port port to send to
     */
    public void buildCommandUDP(String ip, int port) {
        commandStream = new RobotCommandUDPStream(ip, port, ID);
    }

    /**
     * Constructs the vision UDP stream
     *
     * @param ip   ip to to send to
     * @param port port to send to
     */
    public void buildVisionStream(String ip, int port) {
        visionStream = new RobotVisionUDPStream(ip, port, ID);
    }

    /**
     * Constructs the data UDP stream
     *
     * @param port port to receive from
     */
    public void buildDataStream(int port) {
        dataStream = new RobotDataUDPStream(port, ID);
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
    public RobotCommandUDPStream getCommandStream() {
        return commandStream;
    }

    /**
     * @return the vision UDP stream
     */
    public RobotVisionUDPStream getVisionStream() {
        return visionStream;
    }

    /**
     * @return the data UDP stream
     */
    public RobotDataUDPStream getDataStream() {
        return dataStream;
    }
}
