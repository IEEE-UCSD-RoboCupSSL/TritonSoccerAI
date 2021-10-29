package triton.misc;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RWLockee<T> {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private T toLock;

    public RWLockee(T toLock) {
        set(toLock);
    }

    public void set(T toLock) {
        lock.writeLock().lock();
        this.toLock = toLock;
        lock.writeLock().unlock();
    }

    public T get() {
        lock.readLock().lock();
        try {
            return toLock;
        } finally {
            lock.readLock().unlock();
        }
    }

}
