import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

//package Triton.DesignPattern;

public class Subscriber<T> {
    private String topicName, msgName;
    private MsgChannel<T> channel;
    private BlockingQueue<T> queue;
    private boolean useMsgQueue;

    public Subscriber(String topicName, String msgName) {
        this.topicName = topicName;
        this.msgName = msgName;
    }

    public Subscriber(String topicName, String msgName, int queueSize) {
        this.topicName = topicName;
        this.msgName = msgName;
        useMsgQueue = true;
        queue = new LinkedBlockingQueue<T>(queueSize);
    }

    public boolean subscribe() {
        try {
            channel = MsgChannel.getChannel(topicName, msgName);
        } catch (NullPointerException e) {
            return false;
        }

        if (useMsgQueue) {
            channel.addMsgQueue(queue);
        }
        return true;
    }

    public T getLatestMsg() {
        return channel.getMsg();
    }

    public T pollMsg() {
        T rtn = null;
        try {
            rtn = queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public T pollMsg(long ms, T defaultReturn) {
        T rtn = null;
        try {
            rtn = queue.poll(ms, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (rtn == null)
            return defaultReturn;
        return rtn;
    }
}