package Triton.DesignPattern.PubSubSystem;

import java.util.concurrent.TimeoutException;

public abstract class Subscriber<T> {
    protected String topicName, msgName;
    protected MsgChannel<T> channel;

    public Subscriber(String topicName, String msgName) {
        this.topicName = topicName;
        this.msgName = msgName;
    }

    public boolean subscribe() {
        do {
            channel = MsgChannel.getChannel(topicName, msgName);
        } while (channel == null);
        return true;
    }

    public boolean subscribe(long timeout) throws TimeoutException {
        long curr = System.currentTimeMillis();
        do {
            if (System.currentTimeMillis() - curr > timeout) {
                throw new TimeoutException();
            }
            channel = MsgChannel.getChannel(topicName, msgName);
        } while (channel == null);
        return true;
    }

    public abstract T getMsg();
}