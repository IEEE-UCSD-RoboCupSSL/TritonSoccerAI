package Triton.DesignPattern.PubSubSystem;

import java.util.concurrent.*;

public class MQSubscriber<T> extends Subscriber<T> {
    private BlockingQueue<T> queue;

    public MQSubscriber(String topicName, String msgName, int queueSize) {
        super(topicName + "QUEUE", msgName);
        queue = new LinkedBlockingQueue<T>(queueSize);
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
    public boolean subscribe(long timeout) {
        boolean rtn = super.subscribe();
        channel.addMsgQueue(queue);
        return rtn;
    }

    @Override
    public T getMsg() {
        try {
            return (T) queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public T getMsg(long ms, T defaultReturn) {
        T rtn = null;
        try {
            rtn = (T) queue.poll(ms, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (rtn == null)
            return defaultReturn;
        return rtn;
    }
}
