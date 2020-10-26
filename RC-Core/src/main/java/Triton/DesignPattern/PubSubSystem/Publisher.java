package Triton.DesignPattern.PubSubSystem;

public abstract class Publisher<T> {
    protected MsgChannel<T> channel;

    public Publisher (String topicName, String msgName) {   
        channel = new MsgChannel<T>(topicName, msgName);
    }

    public abstract void publish(T msg);
}
