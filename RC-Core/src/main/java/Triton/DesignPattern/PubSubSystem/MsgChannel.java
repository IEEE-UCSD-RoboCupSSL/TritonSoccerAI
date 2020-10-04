package Triton.DesignPattern.PubSubSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

public class MsgChannel<T> {
    private static HashMap<String, MsgChannel> channels = new HashMap<String, MsgChannel>();

    private ReadWriteLock lock;
    private String channelName;
    private T msg;
    private ArrayList<BlockingQueue<T>> queues;

    public MsgChannel(String topicName, String msgName) {
        channelName = topicName + msgName;
        if (channels.containsKey(channelName)) {
            System.out.println("ERROR");
            return;
        }

        lock = new ReentrantReadWriteLock();
        queues = new ArrayList<BlockingQueue<T>>();
        channels.put(channelName, this);
    }

    public static MsgChannel getChannel(String topicName, String msgName) {
        String channelName = topicName + msgName;
        MsgChannel channel = channels.get(channelName);
        if (channel == null)
            throw new NullPointerException();
        return channels.get(channelName);
    }

    public void addMsgQueue(BlockingQueue<T> queue) {
        lock.writeLock().lock();
        try {
            queues.add(queue);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addMsg(T msg) {
        lock.writeLock().lock();
        try {
            this.msg = msg;
            for (BlockingQueue<T> queue : queues) {
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
            this.msg = msg;
            for (BlockingQueue<T> queue : queues) {
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

    public T getMsg() {
        lock.readLock().lock();
        try {
            return msg;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void resetMsg(T msg) {
        lock.writeLock().lock();
        try {
            this.msg = msg;
        } finally {
            lock.writeLock().unlock();
        }
    }
}