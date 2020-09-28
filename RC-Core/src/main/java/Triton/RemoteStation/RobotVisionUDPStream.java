package Triton.RemoteStation;

import java.util.*;

import Triton.DesignPattern.PubSubSystem.Subscriber;
import Triton.Detection.*;

public class RobotVisionUDPStream extends RobotUDPStream {

    private Subscriber<HashMap<Team, HashMap<Integer, RobotData>>> robotSub;
    private Subscriber<BallData> ballSub;

    public RobotVisionUDPStream(String ip, int port, int ID) {
        super(ip, port);

        robotSub = new Subscriber<HashMap<Team, HashMap<Integer, RobotData>>>("detection", "robot");
        ballSub = new Subscriber<BallData>("detection", "ball");
    }
    
}
