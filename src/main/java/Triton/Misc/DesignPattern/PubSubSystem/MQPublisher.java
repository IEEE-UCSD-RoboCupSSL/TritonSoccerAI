package Triton.Misc.DesignPattern.PubSubSystem;

public class MQPublisher<T> extends Publisher<T> {

    public MQPublisher(String topicName, String msgName) {
        super(topicName + "QUEUE", msgName);
    }

    @Override
    public void publish(T msg) {
        channel.addMsg(msg);
    }
    
}
