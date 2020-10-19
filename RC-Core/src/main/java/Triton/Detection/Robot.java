package Triton.Detection;

import java.util.concurrent.ExecutorService;

import Triton.Config.ConnectionConfig;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.DesignPattern.PubSubSystem.Module;
import Triton.RemoteStation.RobotConnection;

public class Robot implements Module {
    private Team team;
    private int ID;
    private ExecutorService pool;

    private RobotData data;
    private RobotConnection conn;

    private Subscriber<RobotData> robotDataSub;

    public Robot(Team team, int ID, ExecutorService pool) {
        this.team = team;
        this.ID = ID;
        this.pool = pool;

        conn = new RobotConnection(team, ID);

        String ip = "";
        int port = 0;

        switch (ID) {
            case 0:
                ip = ConnectionConfig.ROBOT_0_IP.getValue0();
                port = ConnectionConfig.ROBOT_0_IP.getValue1();
                break;
            case 1:
                ip = ConnectionConfig.ROBOT_1_IP.getValue0();
                port = ConnectionConfig.ROBOT_1_IP.getValue1();
                break;
            case 2:
                ip = ConnectionConfig.ROBOT_2_IP.getValue0();
                port = ConnectionConfig.ROBOT_2_IP.getValue1();
                break;
            case 3:
                ip = ConnectionConfig.ROBOT_3_IP.getValue0();
                port = ConnectionConfig.ROBOT_3_IP.getValue1();
                break;
            case 4:
                ip = ConnectionConfig.ROBOT_4_IP.getValue0();
                port = ConnectionConfig.ROBOT_4_IP.getValue1();
                break;
            case 5:
                ip = ConnectionConfig.ROBOT_5_IP.getValue0();
                port = ConnectionConfig.ROBOT_5_IP.getValue1();
                break;
            default:
                System.out.println("Invalid Robot ID");
        }

        conn.buildTcpConnection(ip, port);
        conn.buildCommandUDP(ip, port);
        conn.buildDataStream(ip, port);
        conn.buildVisionStream(ip, port);

        String name = (team == Team.YELLOW) ? "yellow robot data" + ID : "blue robot data" + ID;
        robotDataSub = new FieldSubscriber<RobotData>("detection", name);
    }

    public Team getTeam() {
        return this.team;
    }

    public int getID() {
        return this.ID;
    }

    public RobotData getRobotData() {
        return data;
    }

    public RobotConnection getRobotConnection() {
        return conn;
    }

    @Override
    public void run() {
        robotDataSub.subscribe();

        pool.execute(conn.getTCPConnection());
        pool.execute(conn.getCommandStream());
        pool.execute(conn.getVisionStream());
        pool.execute(conn.getDataStream());
        
        while(true) {
            data = robotDataSub.getMsg();
        }   
    }
}
