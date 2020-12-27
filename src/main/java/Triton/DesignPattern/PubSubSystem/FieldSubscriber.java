package Triton.DesignPattern.PubSubSystem;

public class FieldSubscriber<T> extends Subscriber<T> {

    public FieldSubscriber(String topicName, String msgName) {
        super(topicName + "FIELD", msgName);
    }

    @Override
    public T getMsg() {
        return channel.getMsg();
    }

    public void forceSetMsg(T msg) {
        channel.setMsg(msg);
    }
}
