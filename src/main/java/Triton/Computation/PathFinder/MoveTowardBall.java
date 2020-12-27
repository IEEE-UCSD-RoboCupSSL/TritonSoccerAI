package Triton.Computation.PathFinder;

import Triton.Config.ObjectConfig;
import Triton.DesignPattern.PubSubSystem.Module;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.Detection.BallData;
import Triton.Detection.RobotData;
import Triton.Robot.Robot;
import Triton.Shape.Vec2D;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class MoveTowardBall implements Module {

    private final ArrayList<Subscriber<RobotData>> yellowRobotSubs;
    private final ArrayList<Subscriber<RobotData>> blueRobotSubs;
    private final Subscriber<BallData> ballSub;

    private final ArrayList<Publisher<Pair<Vec2D, Double>>> endPointPubs;

    public MoveTowardBall(Robot robot) {
        yellowRobotSubs = new ArrayList<>();
        blueRobotSubs = new ArrayList<>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobotSubs.add(new FieldSubscriber<>("detection", "yellow robot data" + i));
            blueRobotSubs.add(new FieldSubscriber<>("detection", "blue robot data" + i));
        }
        ballSub = new FieldSubscriber<>("detection", "ball");

        endPointPubs = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            endPointPubs.add(new FieldPublisher<>("endPoint", "" + i, null));
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

            Pair<Vec2D, Double> endPointPair = new Pair<>(ballData.getPos(), 0.0);
            
            for (Publisher<Pair<Vec2D, Double>> endPointPub: endPointPubs) {
                endPointPub.publish(endPointPair);
            }
        }
    }
}
