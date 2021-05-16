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

    /* A special type of publish that spin the full queues so that the publisher thread won't be blocked */
    public void push(T msg) {
        if (channel.isAnyQueueFull()) {
            ArrayList<BlockingQueue<T>> queues = channel.getQueueList();
            for (BlockingQueue<T> queue : queues) {
                if (queue.remainingCapacity() <= 0) {
                    try {
                        /* this will remove the head element of the queue to leave space for this push(i.e. publish) */
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
