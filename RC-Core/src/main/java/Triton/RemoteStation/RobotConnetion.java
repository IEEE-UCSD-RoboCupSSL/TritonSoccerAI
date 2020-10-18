package Triton.RemoteStation;

import java.util.concurrent.ExecutorService;

import Triton.Detection.Team;

public class RobotConnetion {
    private ExecutorService pool;

    private Team team;
    private int ID;

    private RobotTCPConnection tcpConnect;
    private RobotCommandUDPStream commandStream;
    private RobotVisionUDPStream visionStream;
    private RobotInternalUDPStream dataStream;

    public RobotConnetion(Team team, int ID, ExecutorService pool) {
        this.team = team;
        this.ID = ID;
        this.pool = pool;
    }

    public RobotTCPConnection getRobotTCPConnection() {
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
    
    public void buildTcpConnection(String ip, int port) {
        tcpConnect = new RobotTCPConnection(ip, port);
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
}
