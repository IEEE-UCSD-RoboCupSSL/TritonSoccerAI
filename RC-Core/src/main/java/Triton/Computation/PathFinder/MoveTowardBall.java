package Triton.Computation.PathFinder;

import Triton.Config.ObjectConfig;
import Triton.DesignPattern.PubSubSystem.Module;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.Detection.BallData;
import Triton.Detection.Robot;
import Triton.Detection.RobotData;
import Triton.Shape.Vec2D;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class MoveTowardBall implements Module {

    private ArrayList<Subscriber<RobotData>> yellowRobotSubs;
    private ArrayList<Subscriber<RobotData>> blueRobotSubs;
    private Subscriber<BallData> ballSub;

    private ArrayList<Publisher<Pair<Vec2D, Double>>> endPointPubs;

    public MoveTowardBall(Robot robot) {
        yellowRobotSubs = new ArrayList<Subscriber<RobotData>>();
        blueRobotSubs = new ArrayList<Subscriber<RobotData>>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobotSubs.add(new FieldSubscriber<RobotData>("detection", "yellow robot data" + i));
            blueRobotSubs.add(new FieldSubscriber<RobotData>("detection", "blue robot data" + i));
        }
        ballSub = new FieldSubscriber<BallData>("detection", "ball");

        endPointPubs = new ArrayList<Publisher<Pair<Vec2D, Double>>>();
        for (int i = 0; i < 6; i++) {
            endPointPubs.add(new FieldPublisher<Pair<Vec2D, Double>>("endPoint", "" + i, null));
        }
    }

    @Override
    public void run() {
        try {
            for (Subscriber<RobotData> robotSub : yellowRobotSubs)
                robotSub.subscribe(1000);
            for (Subscriber<RobotData> robotSub : blueRobotSubs)
                robotSub.subscribe(1000);
            ballSub.subscribe(1000);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        while (true) {
            BallData ballData = ballSub.getMsg();

            Pair<Vec2D, Double> endPointPair = new Pair<Vec2D, Double>(ballData.getPos(), 0.0);
            
            for (Publisher<Pair<Vec2D, Double>> endPointPub: endPointPubs) {
                endPointPub.publish(endPointPair);
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
