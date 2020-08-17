package Triton.DesignPattern;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public abstract class AbstractData {

    protected String name;
    protected ReadWriteLock lock;
    
    public AbstractData(String name) {
        this.name = name;
        lock = new ReentrantReadWriteLock();
    }

    public void publish() {
        MsgChannel.publish(name, this);
    }
}