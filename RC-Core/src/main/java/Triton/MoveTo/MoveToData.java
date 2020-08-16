package Triton.MoveTo;

import Triton.Detection.Team;
import Triton.Shape.Vec2D;
import Triton.DesignPattern.*;

public class MoveToData extends AbstractData {

    private Team team;
    private int ID;
    private Vec2D des;

    public MoveToData(Team team, int ID, Vec2D des) {
        super();
        lock.writeLock().lock();
        try {
            this.team = team;
            this.ID = ID;
            this.des = des;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Team getTeam() {
        lock.readLock().lock();
        try {
            return team;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getID() {
        lock.readLock().lock();
        try {
            return ID;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Vec2D getDes() {
        lock.readLock().lock();
        try {
            return des;
        } finally {
            lock.readLock().unlock();
        }
    }

    public static MoveToData get() {
        return (MoveToData) MsgChannel.get("MoveTo");
    }

    public static void publish(MoveToData data) {
        MsgChannel.publish("MoveTo", data);
    }

}