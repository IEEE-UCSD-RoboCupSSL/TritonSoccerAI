package Triton.Misc.ModulePubSubSystem;

public abstract class Publisher<T> {
    protected MsgChannel<T> channel;

    public Publisher (String topicName, String msgName) {   
        channel = new MsgChannel<>(topicName, msgName);
    }

    public abstract void publish(T msg);
}
