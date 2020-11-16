package Triton.Computation.PathFinder;

import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

import org.javatuples.Pair;

import Proto.RemoteAPI.Commands;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.DesignPattern.PubSubSystem.Module;
import Triton.Detection.BallData;
import Triton.Detection.RobotData;
import Triton.Detection.Team;
import Triton.Shape.Vec2D;

public class PathRelayer implements Module {
    private Team team;
    private int ID;

    private ThreadPoolExecutor pool;
    private PathRunner runner;
    private Subscriber<Pair<ArrayList<Vec2D>, Double>> pathSub;
    private FieldSubscriber<RobotData> robotDataSub;
    private FieldSubscriber<BallData> ballSub;
    private MQPublisher<Commands> commandsPub;
    private MQPublisher<String> tcpCommandPub;

    public PathRelayer(Team team, int ID, ThreadPoolExecutor pool) {
        this.team = team;
        this.ID = ID;
        this.pool = pool;

        String name = (team == Team.YELLOW) ? "yellow robot data" + ID : "blue robot data" + ID;
        pathSub = new MQSubscriber<Pair<ArrayList<Vec2D>, Double>>("path commands", team.name() + ID);
        robotDataSub = new FieldSubscriber<RobotData>("detection", name);
        ballSub = new FieldSubscriber<BallData>("detection", "ball");

        commandsPub = new MQPublisher<Commands>("commands", "" + ID);
        tcpCommandPub = new MQPublisher<String>("tcpCommand", name);
    }

    public void run() {
        try {
            pathSub.subscribe(1000);
            robotDataSub.subscribe(1000);
            ballSub.subscribe(1000);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        while (true) {
            Pair<ArrayList<Vec2D>, Double> pathWithDir = pathSub.getMsg();

            // End current path runner
            if (runner != null)
                runner.shutdown();

            // Start new one
            runner = new PathRunner(pathWithDir.getValue0(), pathWithDir.getValue1(), robotDataSub, 
                        ballSub, commandsPub, tcpCommandPub);
            pool.submit(runner);
        }
    }
}
