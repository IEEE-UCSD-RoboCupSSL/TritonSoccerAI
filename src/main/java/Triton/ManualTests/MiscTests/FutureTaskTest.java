package Triton.ManualTests.MiscTests;

import Triton.ManualTests.TritonTestable;
import Triton.Misc.ModulePubSubSystem.FieldPublisher;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.PeriodicTask;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class FutureTaskTest implements TritonTestable {

    private double val = 0.0;

    ExecutorService threadPool;
    public FutureTaskTest(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    public boolean test() {

        threadPool.submit(() ->{
            FieldPublisher<Double> valPub = new FieldPublisher<>("FutureTaskTest", "val", val);
            FieldSubscriber<Double> valSub = new FieldSubscriber<>("FutureTaskTest", "val");
            valSub.subscribe();

            ReentrantLock printLock = new ReentrantLock();
            ExampleTask task = new ExampleTask(printLock, valSub);
            FutureTask<Double> futureTask = null;

            boolean isRunning = false;


            ReentrantLock valLock = new ReentrantLock();

            Timer timer = new Timer();

            TimerTask tTask = new PeriodicTask(timer).schedule(()->{
                valLock.lock();
                val += 1.01;
                valLock.unlock();
            }, 100);


            final long t0 = System.currentTimeMillis();

            TimerTask tpTask = new PeriodicTask(timer).schedule(()->{
                printLock.lock();
                System.out.println("Example Main Thread (in the testing part) Running"
                        + "  [current time: " + (System.currentTimeMillis() - t0) + "]");
                printLock.unlock();
            }, 500);


            while(System.currentTimeMillis() - t0 < 9000) {


                if(System.currentTimeMillis() - t0 > 5000) {
                    tTask.cancel();
                }


                if(!isRunning) {
                    futureTask = new FutureTask<>(task);
                    threadPool.submit(futureTask);
                    isRunning = true;
                } else {

                    if(futureTask.isDone()) {
                        isRunning = false;

                        if(futureTask.isCancelled()) {
                            printLock.lock();
                            System.out.println("FutureTask Cancelled");
                            printLock.unlock();
                        } else {

                            double result = 0.0;
                            try {
                                result = futureTask.get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }

                            printLock.lock();
                            System.out.println(">>>>>\n\tExampleTask has finished, result = " + result
                                    + "  [current time: " + (System.currentTimeMillis() - t0) + "]\n");
                            printLock.unlock();
                        }

                    } else {
                        valLock.lock();
                        valPub.publish(val);
                        valLock.unlock();
                    }
                }
            }

            tpTask.cancel();

            System.out.println("!!!!!!!");
        });

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }

    public static class ExampleTask implements Callable<Double> {

        ReentrantLock printLock;
        FieldSubscriber<Double> valSub;
        public ExampleTask(ReentrantLock lock, FieldSubscriber<Double> valSub) {
            this.printLock = lock;
            this.valSub = valSub;
        }

        @Override
        public Double call() throws Exception {

            long t0 = System.currentTimeMillis();
            long tPrev = t0;

            int cnt = 0;

            while(System.currentTimeMillis() - t0 < 3000) {

                if(System.currentTimeMillis() - tPrev > 100) {
                    cnt++;
                    printLock.lock();
                    System.out.println("Example Async Task Running, val: " + (valSub.getMsg() + cnt * 1000.00));
                    printLock.unlock();
                    tPrev = System.currentTimeMillis();
                }

            }

            return valSub.getMsg();
        }
    }




}
