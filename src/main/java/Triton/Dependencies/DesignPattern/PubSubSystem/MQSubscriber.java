package Triton.Dependencies.DesignPattern.PubSubSystem;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MQSubscriber<T> extends Subscriber<T> {
    private final BlockingQueue<T> queue;

    public MQSubscriber(String topicName, String msgName, int queueSize) {
        super(topicName + "QUEUE", msgName);
        queue = new LinkedBlockingQueue<>(queueSize);
    }
	
	// Default size is 1
	public MQSubscriber(String topicName, String msgName) {
		this(topicName, msgName, 1);
	}
    
    @Override
    public boolean subscribe() {
        boolean rtn = super.subscribe();
        channel.addMsgQueue(queue);
        return rtn;
    }

    @Override
    public boolean subscribe(long timeout) throws TimeoutException {
        boolean rtn = super.subscribe(timeout);
        channel.addMsgQueue(queue);
        return rtn;
    }

    @Override
    public T getMsg() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public T getMsg(long ms, T defaultReturn) {
        T rtn = null;
        try {
            rtn = queue.poll(ms, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (rtn == null)
            return defaultReturn;
        return rtn;
    }
}
