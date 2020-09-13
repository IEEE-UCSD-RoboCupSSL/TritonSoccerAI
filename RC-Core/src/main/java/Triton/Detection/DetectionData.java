package Triton.Detection;

import Triton.Config.ObjectConfig;
import Triton.DesignPattern.*;
import Triton.Shape.Vec2D;
import java.util.HashMap;

import java.util.ArrayList;

import Proto.MessagesRobocupSslDetection.SSL_DetectionBall;
import Proto.MessagesRobocupSslDetection.SSL_DetectionRobot;
import Proto.RemoteCommands;
import Proto.RemoteCommands.Remote_Detection;

public class DetectionData extends AbstractData {

    private HashMap<Team, HashMap<Integer, Robot>> robots;
    private int ballCount;
    private Ball ball;
    private double time;
    private double deltaT;

    public DetectionData() {
        super("Detection");
        robots = new HashMap<>();
        robots.put(Team.YELLOW, new HashMap<Integer, Robot>());
        robots.put(Team.BLUE, new HashMap<Integer, Robot>());

        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            robots.get(Team.YELLOW).put(i, new Robot(Team.YELLOW, i));
            robots.get(Team.BLUE).put(i, new Robot(Team.BLUE, i));
        }
        ball = new Ball();
    }

    public void updateTime(double time) {
        lock.writeLock().lock();
        try {
            this.time = time;
            this.deltaT = System.currentTimeMillis() / 1000.0 - time;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public double getTime() {
        lock.readLock().lock();
        try {
            return time;
        } finally {
            lock.readLock().unlock();
        }
    }

    public double getDeltaT() {
        lock.readLock().lock();
        try {
            return deltaT;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getBallCount() {
        lock.readLock().lock();
        try {
            return ballCount;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setBallCount(int ballCount) {
        lock.writeLock().lock();
        try {
            this.ballCount = ballCount;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Ball getBall() {
        lock.readLock().lock();
        try {
            return ball;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void updateBall(SSL_DetectionBall ball, double time) {
        lock.writeLock().lock();
        try {
            this.ball.update(ball, time);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Vec2D getBallPos() {
        lock.readLock().lock();
        try {
            return ball.getPos();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Vec2D getBallVel() {
        lock.readLock().lock();
        try {
            return ball.getVel();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Robot getRobot(Team team, int ID) {
        lock.readLock().lock();
        try {
            return robots.get(team).get(ID);
        } finally {
            lock.readLock().unlock();
        }
    }

    public HashMap<Team, HashMap<Integer, Robot>> getRobots() {
        lock.readLock().lock();
        try {
            return robots;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void updateRobot(Team team, int ID, SSL_DetectionRobot detection, double time) {
        lock.writeLock().lock();
        try {
            if (ID < ObjectConfig.ROBOT_COUNT)
                getRobot(team, ID).update(detection, time);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Vec2D getRobotPos(Team team, int ID) {
        lock.readLock().lock();
        try {
            return getRobot(team, ID).getPos();
        } finally {
            lock.readLock().unlock();
        }
    }

    public double getRobotOrient(Team team, int ID) {
        lock.readLock().lock();
        try {
            return getRobot(team, ID).getOrient();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Vec2D getRobotVel(Team team, int ID) {
        lock.readLock().lock();
        try {
            return getRobot(team, ID).getVel();
        } finally {
            lock.readLock().unlock();
        }
    }

    public double getRobotAngularVelocity(Team team, int ID) {
        lock.readLock().lock();
        try {
            return getRobot(team, ID).getAngularVelocity();
        } finally {
            lock.readLock().unlock();
        }
    }

    public double getRobotHeight(Team team, int ID) {
        lock.readLock().lock();
        try {
            return getRobot(team, ID).getHeight();
        } finally {
            lock.readLock().unlock();
        }
    }

    public static DetectionData get() {
        return (DetectionData) MsgChannel.get("Detection");
    }

    public Remote_Detection toProto() {
        Remote_Detection.Builder toSend = Remote_Detection.newBuilder();

        toSend.setBallPos(getBallPos().toProto());
        toSend.setBallVel(getBallVel().toProto());

        ArrayList<RemoteCommands.Vec2D> bPos = new ArrayList<RemoteCommands.Vec2D>();
        ArrayList<Double> bOri = new ArrayList<Double>();
        ArrayList<RemoteCommands.Vec2D> bVel = new ArrayList<RemoteCommands.Vec2D>();

        ArrayList<RemoteCommands.Vec2D> yPos = new ArrayList<RemoteCommands.Vec2D>();
        ArrayList<Double> yOri = new ArrayList<Double>();
        ArrayList<RemoteCommands.Vec2D> yVel = new ArrayList<RemoteCommands.Vec2D>();

        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            bPos.add(getRobotPos(Team.BLUE, i).toProto());
            bOri.add(getRobotOrient(Team.BLUE, i));
            bVel.add(getRobotVel(Team.BLUE, i).toProto());

            yPos.add(getRobotPos(Team.YELLOW, i).toProto());
            yOri.add(getRobotOrient(Team.YELLOW, i));
            yVel.add(getRobotVel(Team.YELLOW, i).toProto());
        }

        toSend.addAllBPos(bPos);
        toSend.addAllBVel(bVel);
        toSend.addAllBOri(bOri);

        toSend.addAllYPos(yPos);
        toSend.addAllYVel(yVel);
        toSend.addAllYOri(yOri);

        return toSend.build();
    }
}