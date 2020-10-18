package Triton.DesignPattern.PubSubSystem;

public class FieldPublisher<T> extends Publisher<T> {

    public FieldPublisher(String topicName, String msgName, T defaultMsg) {
       super(topicName + "FIELD", msgName);
       publish(defaultMsg);
    }

    @Override
    public void publish(T msg) {
        channel.setMsg(msg);
    }
}
