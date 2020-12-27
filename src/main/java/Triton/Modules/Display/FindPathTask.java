package Triton.Modules.Display;

import Triton.Algorithms.PathFinder.PathFinder;
import Triton.Config.ObjectConfig;
import Triton.Dependencies.DesignPattern.PubSubSystem.FieldSubscriber;
import Triton.Dependencies.DesignPattern.PubSubSystem.Subscriber;
import Triton.Modules.Detection.BallData;
import Triton.Modules.Detection.RobotData;
import Triton.Dependencies.Shape.Circle2D;
import Triton.Dependencies.Shape.Vec2D;

import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

class FindPathTask extends TimerTask {
    private final Display display;
    private final PathFinder pathfinder;
    private final ArrayList<Subscriber<RobotData>> yellowRobotSubs;
    private final ArrayList<Subscriber<RobotData>> blueRobotSubs;
    private final Subscriber<BallData> ballSub;

    /* If unspecified, find path from the closest robot to ball */
    private Vec2D start;
    private Vec2D dest;

    public FindPathTask(Display display, PathFinder pathfinder) {
        this.display = display;
        this.pathfinder = pathfinder;

        yellowRobotSubs = new ArrayList<>();
        blueRobotSubs = new ArrayList<>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobotSubs.add(new FieldSubscriber<>("detection", "yellow robot data" + i));
            blueRobotSubs.add(new FieldSubscriber<>("detection", "blue robot data" + i));
        }

        ballSub = new FieldSubscriber<>("detection", "ball");
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

        ArrayList<RobotData> blueRobots = new ArrayList<>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            blueRobots.add(blueRobotSubs.get(i).getMsg());
        }

        ArrayList<RobotData> yellowRobots = new ArrayList<>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobots.add(yellowRobotSubs.get(i).getMsg());
        }

        BallData ball = ballSub.getMsg();
        boolean customPath = start != null && dest != null;

        /* Calculate the closest blue robot to the ball */
        RobotData closestRobot = null;
        Vec2D ballPos = ball.getPos();
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < 6; i++) {
            RobotData robot = blueRobots.get(i);
            Vec2D robotPos = robot.getPos();
            double dist = Vec2D.dist2(ballPos, robotPos);
            if (dist < minDist) {
                closestRobot = robot;
                minDist = dist;
            }
        }
        Vec2D closestRobotPos = closestRobot.getPos();

        /* Add all (other) robots as obstacles */
        ArrayList<Circle2D> obstacles = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            RobotData robot = yellowRobots.get(i);
            if (!customPath && robot == closestRobot) {
                continue;
            }
            obstacles.add(new Circle2D(robot.getPos(), ObjectConfig.ROBOT_RADIUS));
        }
        for (int i = 0; i < 6; i++) {
            RobotData robot = blueRobots.get(i);
            if (!customPath && robot == closestRobot) {
                continue;
            }
            obstacles.add(new Circle2D(robot.getPos(), ObjectConfig.ROBOT_RADIUS));
        }

        pathfinder.setObstacles(obstacles);
        if (customPath) {
            long t0 = System.currentTimeMillis();
            ArrayList<Vec2D> path = pathfinder.findPath(start, dest);
            long t1 = System.currentTimeMillis();
            System.out.println(pathfinder.getName() + " takes " + (t1 - t0) + " ms");

            display.setPath(path);
        } else {
            display.setPath(pathfinder.findPath(closestRobotPos, ballPos));
        }
    }

    public void setEnds(Vec2D start, Vec2D dest) {
        this.start = start;
        this.dest = dest;
    }
}
