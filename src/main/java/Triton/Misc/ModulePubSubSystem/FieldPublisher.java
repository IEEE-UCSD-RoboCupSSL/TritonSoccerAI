package Triton.Misc.ModulePubSubSystem;

public class FieldPublisher<T> extends Publisher<T> {

    public FieldPublisher(String topicName, String msgName, T defaultMsg) {
        super(topicName + "FIELD", msgName);
        publish(defaultMsg);

        try {
            Thread.sleep(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void publish(T msg) {
        channel.setMsg(msg);
    }
}
