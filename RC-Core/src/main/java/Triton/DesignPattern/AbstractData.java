package Triton.DesignPattern;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public abstract class AbstractData {

    protected ReadWriteLock lock;
    
    public AbstractData() {
        lock = new ReentrantReadWriteLock();
    }
}