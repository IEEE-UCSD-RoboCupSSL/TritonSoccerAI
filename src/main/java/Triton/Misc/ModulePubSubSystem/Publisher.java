package Triton.Misc.ModulePubSubSystem;

/**
 * Publisher of the Publisher-Subscriber pattern.
 *
 * @param <T> The type of the content to be delivered.
 */
public abstract class Publisher<T> {
    protected MsgChannel<T> channel;

    /**
     * Construct a channel for the given topicName and msgName.
     *
     * @param topicName The topic name of the channel.
     * @param msgName THe message name of the channel.
     */
    public Publisher(String topicName, String msgName) {
        channel = new MsgChannel<>(topicName, msgName);
    }

    public abstract void publish(T msg);
}
