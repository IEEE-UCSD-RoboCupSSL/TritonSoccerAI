package Triton.Misc.ModulePubSubSystem;

public class FieldSubscriber<T> extends Subscriber<T> {

    public FieldSubscriber(String topicName, String msgName) {
        super(topicName + "FIELD", msgName);
    }

    @Override
    public T getMsg() {
        if (!subscriptionFlag)
            System.out.println("NOT SUBSCRIBED");

        T msg = channel.getMsg();
//        try {
//            Thread.sleep(1);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return msg;
    }

    public void forceSetMsg(T msg) {
        channel.setMsg(msg);
    }
}
