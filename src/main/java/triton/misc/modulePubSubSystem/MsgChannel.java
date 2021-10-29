package triton.misc.modulePubSubSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MsgChannel<T> {
    private static final HashMap<String, MsgChannel> channels = new HashMap<>();
    private final String channelName;
    private ReadWriteLock lock;
    private T msg;
    private ArrayList<BlockingQueue<T>> queueList;


    public static class ChannelAlreadyRegisteredException extends Exception {
        public ChannelAlreadyRegisteredException() {super();}
    }


    public MsgChannel(String topicName, String msgName) throws ChannelAlreadyRegisteredException {
        channelName = topicName + msgName;
        if (channels.containsKey(channelName)) {
            throw new ChannelAlreadyRegisteredException();
        }

        lock = new ReentrantReadWriteLock();
        queueList = new ArrayList<>();
        channels.put(channelName, this);
    }

    public static MsgChannel getChannel(String topicName, String msgName) {
        String channelName = topicName + msgName;
        MsgChannel channel = channels.get(channelName);
        return channels.get(channelName);
    }

    public void addMsgQueue(BlockingQueue<T> queue) {
        lock.writeLock().lock();
        try {
            queueList.add(queue);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public T getMsg() {
        lock.readLock().lock();
        try {
            return msg;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setMsg(T msg) {
        lock.writeLock().lock();
        try {
            this.msg = msg;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addMsg(T msg) {
        lock.writeLock().lock();
        try {
            for (BlockingQueue<T> queue : queueList) {
                try {
                    queue.put(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addMsg(T msg, long timeout_ms) {
        lock.writeLock().lock();
        try {
            for (BlockingQueue<T> queue : queueList) {
                try {
                    queue.offer(msg, timeout_ms, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isAnyQueueFull() {
        for (BlockingQueue<T> queue : queueList) {
            if (queue.remainingCapacity() <= 0) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<BlockingQueue<T>> getQueueList() {
        return queueList;
    }
}