package Triton.Misc.ModulePubSubSystem;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class MQPublisher<T> extends Publisher<T> {

    public MQPublisher(String topicName, String msgName) {
        super(topicName + "QUEUE", msgName);
    }

    @Override
    public void publish(T msg) {
        channel.addMsg(msg);
    }

    public void publish(T msg, long ms) {
        channel.addMsg(msg, ms);
    }

    public void forcePublish(T msg) {
        if (channel.isAnyQueueFull()) {
            ArrayList<BlockingQueue<T>> queues = channel.getQueues();
            for (BlockingQueue<T> queue : queues) {
                if (!queue.isEmpty()) {
                    try {
                        queue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        publish(msg);
    }
}
