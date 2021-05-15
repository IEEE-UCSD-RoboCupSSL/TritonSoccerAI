package Triton.Misc.ModulePubSubSystem;

public abstract class Publisher<T> {
    protected MsgChannel<T> channel;

    public Publisher(String topicName, String msgName) {
        try {
            channel = new MsgChannel<>(topicName, msgName);
        } catch (MsgChannel.ChannelAlreadyRegisteredException e) {
            // System.out.println("????????????????????????????????????????????");
            channel = MsgChannel.getChannel(topicName, msgName);
        }
    }

    public abstract void publish(T msg);
}
