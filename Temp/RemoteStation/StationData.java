package Triton.RemoteStation;

import java.util.HashMap;
import Triton.DesignPattern.*;

public class StationData extends AbstractData {
    
    private HashMap<Integer, Integer> ports = new HashMap<Integer, Integer>();

    public StationData() {
        super("Station");
    }

    public int getNumRobot() {
        lock.readLock().lock();
        try {
            return ports.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void putPort(int robotID, int port) {
        lock.writeLock().lock();
        try {
            ports.put(robotID, port);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int getPort(int robotID) {
        lock.readLock().lock();
        try {
            return ports.get(robotID);
        } finally {
            lock.readLock().unlock();
        }
    }

    public static StationData get() {
        return (StationData) MsgChannel.get("Station");
    }
}