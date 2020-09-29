package Triton.RemoteStation;

import java.util.concurrent.ExecutorService;

import Triton.Detection.Team;

public class RobotConnetion {
    private ExecutorService pool;

    private Team team;
    private int ID;

    private RobotTCPConnection tcpConnect;
    private RobotUDPStream commandStream;
    private RobotUDPStream visionStream;
    private RobotUDPStream dataStream;

    public RobotConnetion(Team team, int ID, ExecutorService pool) {
        this.team = team;
        this.ID = ID;
        this.pool = pool;
    }

    public void buildTcpConnection(String ip, int port) {
        tcpConnect = new RobotTCPConnection(ip, port);
    }

    public void buildCommandUDP(String ip, int port) {
        commandStream = new RobotUDPStream(ip, port, team, ID);
    }

    public void buildVisionStream(String ip, int port) {
        visionStream = new RobotUDPStream(ip, port, team, ID);
    }

    public void buildDataStream(String ip, int port) {
        dataStream = new RobotUDPStream(ip, port, team, ID);
    }
}
