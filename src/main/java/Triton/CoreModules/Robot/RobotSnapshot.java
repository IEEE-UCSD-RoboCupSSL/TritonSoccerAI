package Triton.CoreModules.Robot;


import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.Math.Matrix.Vec2D;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RobotSnapshot {

    public final ReadWriteLock lock;
    private Team team;
    private int ID;
    private Vec2D pos;
    private Vec2D vel;
    private double dir;

    public void update(Robot robot) {
        this.lock.writeLock().lock();
        this.team = robot.team;
        this.ID = robot.ID;
        this.pos = robot.getPos();
        this.vel = robot.getVel();
        this.dir = robot.getDir();
        this.lock.writeLock().unlock();
    }

    public RobotSnapshot(Robot robot) {
        this.lock = new ReentrantReadWriteLock();
        update(robot);
    }

    public Team getTeam() {
        this.lock.readLock().lock();
        try {
            return team;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public int getID() {
        this.lock.readLock().lock();
        try {
            return ID;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public Vec2D getPos() {
        this.lock.readLock().lock();
        try {
            return pos;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public Vec2D getVel() {
        this.lock.readLock().lock();
        try {
            return vel;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public double getDir() {
        this.lock.readLock().lock();
        try {
            return dir;
        } finally {
            this.lock.readLock().unlock();
        }
    }
}
