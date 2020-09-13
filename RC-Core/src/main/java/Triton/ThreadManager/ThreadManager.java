package Triton.ThreadManager;

import java.util.HashMap;

public class ThreadManager {
    private static ThreadManager threadManger;
    private HashMap<String, Thread> threads;

    public ThreadManager() {
        threads = new HashMap<String, Thread>();
    }

    public static ThreadManager getManager() {
        if (threadManger == null) {
            threadManger = new ThreadManager();
            return threadManger;
        }
        return threadManger;
    }

    public Thread getThread(String name) {
        return threads.get(name);
    }

    public void addThread(Thread thread, String name) {
        threads.put(name, thread);
    }

    public void startThread(String name) {
        threads.get(name).start();
    }

    public void notifyThread(String name) {
        threads.get(name).notify();
    }

    public void waitThread(String name) {
        try {
            threads.get(name).wait();
        } catch (Exception e) {
            // Do nothing
        }
    }
}
