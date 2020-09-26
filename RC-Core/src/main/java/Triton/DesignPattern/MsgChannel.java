package Triton.DesignPattern;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MsgChannel<T> {
    private static HashMap<String, MsgChannel> channels;

    private ReadWriteLock lock;
    private String channelName;
    private T msg;

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
        channels.put(channelName, this);
    }

    public static MsgChannel getChannel(String topicName, String msgName) {
        String channelName = topicName + msgName;
        MsgChannel channel = channels.get(channelName);
        if (channel == null)
            throw new NullPointerException();
        return channels.get(channelName);
    }

    public void addMsg(T msg) {
        lock.writeLock().lock();
        try {
            this.msg = msg;
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
            Subscriber<Integer> sub = new Subscriber<Integer>("Test", "int");
            while(!sub.subscribe());
             
            while(true) {
                System.out.println(sub.getLatestMsg());
            }

        });
        subThread.start();
    }
}
