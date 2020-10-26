package Triton.Computation;

import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

import org.javatuples.Pair;

import Triton.DesignPattern.PubSubSystem.*;
import Triton.DesignPattern.PubSubSystem.Module;
import Triton.Detection.Team;
import Triton.Shape.Vec2D;
import io.grpc.netty.shaded.io.netty.util.concurrent.Future;

public class PathRelayer implements Module {
    private Team team;
    private int ID;

    private ThreadPoolExecutor pool;
    private PathRunner runner;
    private Future<?> runnerFuture;
    private Subscriber<Pair<ArrayList<Vec2D>, Double>> pathSub;

    public PathRelayer(Team team, int ID, ThreadPoolExecutor pool) {
        this.team = team;
        this.ID = ID;
        this.pool = pool;
        pathSub = new MQSubscriber<Pair<ArrayList<Vec2D>, Double>>("path commands", team.name() + ID, 1);
    }

    public void run() {
        pathSub.subscribe();

        while (true) {
            Pair<ArrayList<Vec2D>, Double> pathWithDir = pathSub.getMsg();
            
            if (runnerFuture != null) {
                runnerFuture.cancel(true);
            }

            runner = new PathRunner(team, ID, pathWithDir.getValue0(), pathWithDir.getValue1());
            runnerFuture = (Future<?>) pool.submit(runner);
        }
    }
}
