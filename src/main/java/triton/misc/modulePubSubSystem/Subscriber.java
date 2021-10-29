package triton.misc.modulePubSubSystem;

import java.util.concurrent.TimeoutException;

public abstract class Subscriber<T> {
    protected String topicName, msgName;
    protected MsgChannel<T> channel;
    protected boolean subscriptionFlag = false;

    public Subscriber(String topicName, String msgName) {
        this.topicName = topicName;
        this.msgName = msgName;
    }

    public boolean subscribe() {
        do {
            channel = MsgChannel.getChannel(topicName, msgName);
        } while (channel == null);
        subscriptionFlag = true;
        return true; // meaningless return, just ignore it
    }

    public boolean subscribe(long timeout) throws TimeoutException {
        long curr = System.currentTimeMillis();
        do {
            if (System.currentTimeMillis() - curr > timeout) {
                throw new TimeoutException();
            }

            channel = MsgChannel.getChannel(topicName, msgName);
        } while (channel == null);
        subscriptionFlag = true;
        return true;
    }

    public boolean isSubscribed() {
        return subscriptionFlag;
    }


    public abstract T getMsg();
}