package Triton.Computation.PathFinder;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import Triton.Config.ObjectConfig;
import Triton.DesignPattern.PubSubSystem.FieldSubscriber;
import Triton.DesignPattern.PubSubSystem.Module;
import Triton.DesignPattern.PubSubSystem.Subscriber;
import Triton.Detection.BallData;
import Triton.Detection.Robot;
import Triton.Detection.RobotData;

public class MoveTowardBall implements Module {

    private ArrayList<Subscriber<RobotData>> yellowRobotSubs;
    private ArrayList<Subscriber<RobotData>> blueRobotSubs;
    private Subscriber<BallData> ballSub;

    private Robot robot;

    public MoveTowardBall(Robot robot) {
        this.robot = robot;

        yellowRobotSubs = new ArrayList<Subscriber<RobotData>>();
        blueRobotSubs = new ArrayList<Subscriber<RobotData>>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobotSubs.add(new FieldSubscriber<RobotData>("detection", "yellow robot data" + i));
            blueRobotSubs.add(new FieldSubscriber<RobotData>("detection", "blue robot data" + i));
        }
        ballSub = new FieldSubscriber<BallData>("detection", "ball");
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
            robot.setEndPoint(ballData.getPos(), 0);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
