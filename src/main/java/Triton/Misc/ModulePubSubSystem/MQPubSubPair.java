package Triton.Misc.ModulePubSubSystem;

public class MQPubSubPair<T> {

    public final MQPublisher<T> pub;
    public final MQSubscriber<T> sub;


    public MQPubSubPair(String topicName, String msgName) {
        pub = new MQPublisher<>(topicName, msgName);
        sub = new MQSubscriber<>(topicName, msgName);
        sub.subscribe();
    }

    public MQPubSubPair(String topicName, String msgName, int queueSize) {
        pub = new MQPublisher<>(topicName, msgName);
        sub = new MQSubscriber<>(topicName, msgName, queueSize);
        sub.subscribe();
    }

}
