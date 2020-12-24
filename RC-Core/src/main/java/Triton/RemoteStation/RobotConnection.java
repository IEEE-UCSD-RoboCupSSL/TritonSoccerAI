package Triton.RemoteStation;

public class RobotConnection {
    private final int ID;

    private RobotTCPConnection tcpConnect;
    private RobotCommandUDPStream commandStream;
    private RobotVisionUDPStream visionStream;
    private RobotDataUDPStream dataStream;

    public RobotConnection(int ID) {
        this.ID = ID;
    }

    public void buildTcpConnection(String ip, int port) {
        tcpConnect = new RobotTCPConnection(ip, port, ID);
    }

    public void buildCommandUDP(String ip, int port) {
        commandStream = new RobotCommandUDPStream(ip, port, ID);
    }

    public void buildVisionStream(String ip, int port) {
        visionStream = new RobotVisionUDPStream(ip, port, ID);
    }

    public void buildDataStream(int port) {
        dataStream = new RobotDataUDPStream(port, ID);
    }

    public RobotTCPConnection getTCPConnection() {
        return tcpConnect;
    }

    public RobotCommandUDPStream getCommandStream() {
        return commandStream;
    }

    public RobotVisionUDPStream getVisionStream() {
        return visionStream;
    }

    public RobotDataUDPStream getDataStream() {
        return dataStream;
    }
    

}
