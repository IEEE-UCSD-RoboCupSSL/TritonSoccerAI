package Triton.RemoteStation;

import java.util.concurrent.ExecutorService;

import Triton.Detection.*;

public class RobotConnection {
    private Team team;
    private int ID;

    private RobotTCPConnection tcpConnect;
    private RobotCommandUDPStream commandStream;
    private RobotVisionUDPStream visionStream;
    private RobotInternalUDPStream dataStream;

    public RobotConnection(Team team, int ID) {
        this.team = team;
        this.ID = ID;
    }

    public void buildTcpConnection(String ip, int port) {
        tcpConnect = new RobotTCPConnection(ip, port, team, ID);
    }

    public void buildCommandUDP(String ip, int port) {
        commandStream = new RobotCommandUDPStream(ip, port, team, ID);
    }

    public void buildVisionStream(String ip, int port) {
        visionStream = new RobotVisionUDPStream(ip, port, team, ID);
    }

    public void buildDataStream(String ip, int port) {
        dataStream = new RobotInternalUDPStream(ip, port, team, ID);
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

    public RobotInternalUDPStream getDataStream() {
        return dataStream;
    }
    

}
