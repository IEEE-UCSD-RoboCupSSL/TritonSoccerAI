package Triton.CoreModules.Robot;

import Triton.Config.ObjectConfig;
import Triton.Misc.Math.Matrix.Vec2D;

import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A thread-safe, static snapshot of robots' status
 */
public class RobotSnapshot {

    private final ArrayList<Vec2D>  vel;
    private final ArrayList<Vec2D>  pos;
    private final ArrayList<Double> dir;
    private final RobotList<? extends Robot> robots;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public RobotSnapshot(RobotList<? extends Robot> robots) {
        this.robots = robots;
        vel = new ArrayList<>(robots.size());
        pos = new ArrayList<>(robots.size());
        dir = new ArrayList<>(robots.size());
        update();
    }

    public void update() {
        lock.writeLock().lock();
        for (Robot robot : robots) {
            vel.set(robot.getID(), robot.getVel());
            pos.set(robot.getID(), robot.getPos());
            dir.set(robot.getID(), robot.getDir());
        }
        lock.writeLock().unlock();
    }

    public Vec2D getVel(int ID) {
        lock.readLock().lock();
        try {
            return vel.get(ID);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Vec2D getPos(int ID) {
        lock.readLock().lock();
        try {
            return pos.get(ID);
        } finally {
            lock.readLock().unlock();
        }
    }

    public double getDir(int ID) {
        lock.readLock().lock();
        try {
            return dir.get(ID);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean ready() {
        lock.readLock().lock();
        try {
            for (Robot robot : robots) {
                if (robot.getPos() == null | robot.getVel() == null) {
                    return false;
                }
            }
            return true;
        } finally {
            lock.readLock().unlock();
        }
    }
}
