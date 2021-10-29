package triton.misc.modulePubSubSystem;

import lombok.Data;

@Data
public class FieldPubSubPair<T> {

    public final FieldPublisher<T> pub;
    public final FieldSubscriber<T> sub;

    public FieldPubSubPair(String topicName, String msgName, T defaultMsg) {
        pub = new FieldPublisher<>(topicName, msgName, defaultMsg);
        sub = new FieldSubscriber<>(topicName, msgName);
        sub.subscribe();
    }

}
