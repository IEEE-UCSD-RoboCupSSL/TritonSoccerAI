package Triton.DesignPattern;

public class Subscriber<T> {
    private String topicName, msgName;
    private MsgChannel<T> channel;
    private boolean useMsgQueue;
    public Subscriber (String topicName, String msgName) {
        this.topicName = topicName; 
        this.msgName = msgName;
    }

    public boolean subscribe() {
        channel = MsgChannel.getChannel(topicName, msgName);
        if (channel == null)  {
            return false;
        }
        if (useMsgQueue) {
            // TODO
        }
        return true;
    }

    public T getLatestMsg() {
        return channel.getMsg();
    }
}

