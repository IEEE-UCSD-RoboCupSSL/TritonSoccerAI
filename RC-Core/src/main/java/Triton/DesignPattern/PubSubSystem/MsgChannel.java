package Triton.DesignPattern.PubSubSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MsgChannel<T> {
    private static HashMap<String, MsgChannel> channels;

    private ReadWriteLock lock;
    private String channelName;
    private T msg;
    private ArrayList<BlockingQueue<T>> queues;

    static {
        channels = new HashMap<String, MsgChannel>();
    }

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

    public static void main(String[] args) {
        Thread pubThread = new Thread(() -> {
            Publisher<Integer> pub = new Publisher<Integer>("Test", "int");

            for(int i = 0; i < 100; i++) {
                pub.publish(i);
            }

        });
        pubThread.start();

        Thread subThread = new Thread(() -> {
            Subscriber<Integer> sub = new Subscriber<Integer>("Test", "int", 10);
            while(!sub.subscribe());
             
            for(int i = 0; i < 100; i++) {
                System.out.println(sub.pollMsg());
            }

        });
        subThread.start();
    }
}
