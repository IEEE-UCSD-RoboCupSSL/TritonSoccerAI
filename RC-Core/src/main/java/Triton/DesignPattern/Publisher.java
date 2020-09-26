package Triton.DesignPattern;

public class Publisher<T> {
    private MsgChannel<T> channel;

    public Publisher (String topicName, String msgName) {   
        channel = new MsgChannel<T>(topicName, msgName);
    }

    public Publisher () {   
        channel = null;
    }

    public void publish(T msg) {
        channel.addMsg(msg);
    }

}
