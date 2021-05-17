package Triton.ManualTests.MiscTests;

import Triton.ManualTests.TritonTestable;
import Triton.Misc.ModulePubSubSystem.FieldPubSubPair;
import Triton.Misc.ModulePubSubSystem.MQPubSubPair;
import Triton.Util;

import java.util.Scanner;
import java.util.concurrent.*;

public class PubSubTests implements TritonTestable {
    ScheduledExecutorService threadPool;

    public PubSubTests(ScheduledExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    public boolean test() {
        Scanner scanner = new Scanner(System.in);

        boolean quit = false;
        String cmd;
        while(!quit) {
            System.out.println(">>> Enter 'async'(field pub/sub), 'sync'(message queue pub/sub), or 'q'(to quit)");
            cmd = scanner.nextLine();
            switch (cmd) {
                case "q" -> quit = true;
                case "async" -> {
                    if(!asyncOnePubToOneSub()) return false;
                    if(!asyncOnePubToManySub()) return false;
                    if(!asyncManyPubToOneSub()) return false;
                    if(!asyncManyPubToManySub()) return false;
                }
                case "sync" -> {
                    System.out.println(">>> Enter queue size");
                    int size = scanner.nextInt();
                    if(!syncOnePubToOneSub(size)) return false;
                    if(!syncOnePubToManySub(size)) return false;
                    if(!syncManyPubToOneSub(size)) return false;
                    if(!syncManyPubToManySub(size)) return false;
                }
                default -> System.out.println("Unknown cmd");
            }
        }
        return true;
    }


    /*** Async/Non-blocking/Field PubSub ***/

    private boolean asyncOnePubToOneSub() {
        final FieldPubSubPair<String> xxxPubSub =
                new FieldPubSubPair<>("PubSubTests", "xxx", "");
        long threadAFreqInHz = 1000;
        long threadBFreqInHz = 100;

        // Thread A - publishing to xxxPubSub
        ScheduledFuture<?> threadAFuture = threadPool.scheduleAtFixedRate(()->{
            xxxPubSub.pub.publish("Async 1-1  Thread A: " + System.currentTimeMillis());
        }, 0, Util.toPeriod(threadAFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);


        // ThreadB - subscribed to xxxPubSub
        ScheduledFuture<?> threadBFuture = threadPool.scheduleAtFixedRate(()->{
            System.out.println(xxxPubSub.sub.getMsg());
        }, 0, Util.toPeriod(threadBFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadAFuture.cancel(false);
        threadBFuture.cancel(false);
        return true;
    }


    private boolean asyncOnePubToManySub() {
        final FieldPubSubPair<String> xxxPubSub =
                new FieldPubSubPair<>("PubSubTests", "xxx", "");
        long threadAFreqInHz = 1000;
        long threadBFreqInHz = 100;

        // Thread A - publishing to xxxPubSub
        ScheduledFuture<?> threadAFuture = threadPool.scheduleAtFixedRate(()->{
            xxxPubSub.pub.publish("Async 1-m  thread A : " + System.currentTimeMillis());
        }, 0, Util.toPeriod(threadAFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        // ThreadB - subscribed to xxxPubSub
        ScheduledFuture<?> threadBFuture = threadPool.scheduleAtFixedRate(()->{
            System.out.println(xxxPubSub.getSub().getMsg());
        }, 0, Util.toPeriod(threadBFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        // ThreadC - subscribed to xxxPubSub
        ScheduledFuture<?> threadCFuture = threadPool.scheduleAtFixedRate(()->{
            System.out.println(xxxPubSub.getSub().getMsg());
        }, 0, Util.toPeriod(threadBFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadAFuture.cancel(false);
        threadBFuture.cancel(false);
        threadCFuture.cancel(false);
        return true;

    }


    private boolean asyncManyPubToOneSub() {
        final FieldPubSubPair<String> xxxPubSub =
                new FieldPubSubPair<>("PubSubTests", "xxx", "");
        long threadAFreqInHz = 1000;
        long threadBFreqInHz = 100;

        // Thread A - publishing to xxxPubSub
        ScheduledFuture<?> threadAFuture = threadPool.scheduleAtFixedRate(()->{
            xxxPubSub.pub.publish("Async m-1  thread A : " + System.currentTimeMillis());
        }, 0, Util.toPeriod(threadAFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        // ThreadB - publishing to xxxPubSub
        ScheduledFuture<?> threadBFuture = threadPool.scheduleAtFixedRate(()->{
            xxxPubSub.pub.publish("Async m-1  thread B : " + System.currentTimeMillis());
        }, 0, Util.toPeriod(threadBFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        // ThreadC - subscribed to xxxPubSub
        ScheduledFuture<?> threadCFuture = threadPool.scheduleAtFixedRate(()->{
            System.out.println(xxxPubSub.getSub().getMsg());
        }, 0, Util.toPeriod(threadBFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadAFuture.cancel(false);
        threadBFuture.cancel(false);
        threadCFuture.cancel(false);
        return true;
    }


    private boolean asyncManyPubToManySub() {
        final FieldPubSubPair<String> xxxPubSub =
                new FieldPubSubPair<>("PubSubTests", "xxx", "");
        long threadAFreqInHz = 1000;
        long threadBFreqInHz = 100;

        // Thread A - publishing to xxxPubSub
        ScheduledFuture<?> threadAFuture = threadPool.scheduleAtFixedRate(()->{
            xxxPubSub.pub.publish("Async m-m  thread A : " + System.currentTimeMillis());
        }, 0, Util.toPeriod(threadAFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        // ThreadB - publishing to xxxPubSub
        ScheduledFuture<?> threadBFuture = threadPool.scheduleAtFixedRate(()->{
            xxxPubSub.pub.publish("Async m-m  thread B : " + System.currentTimeMillis());
        }, 0, Util.toPeriod(threadBFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        // ThreadC - subscribed to xxxPubSub
        ScheduledFuture<?> threadCFuture = threadPool.scheduleAtFixedRate(()->{
            System.out.println(xxxPubSub.getSub().getMsg());
        }, 0, Util.toPeriod(threadBFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        // ThreadD - subscribed to xxxPubSub
        ScheduledFuture<?> threadDFuture = threadPool.scheduleAtFixedRate(()->{
            System.out.println(xxxPubSub.getSub().getMsg());
        }, 0, Util.toPeriod(threadBFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadAFuture.cancel(false);
        threadBFuture.cancel(false);
        threadCFuture.cancel(false);
        threadDFuture.cancel(false);
        return true;
    }






    /*** Sync/EmptyFullBlocking/MessageQueue PubSub ***/

    private boolean syncOnePubToOneSub(int queueSize) {
        final MQPubSubPair<String> xxxPubSub =
                new MQPubSubPair<>("PubSubTests", "xxx", 10);
        long threadAFreqInHz = 1000;
        long threadBFreqInHz = 100;

        // Thread A - publishing to xxxPubSub
        ScheduledFuture<?> threadAFuture = threadPool.scheduleAtFixedRate(()->{
            xxxPubSub.pub.publish("Sync 1-1  thread A : " + System.currentTimeMillis());
        }, 0, Util.toPeriod(threadAFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);


        // ThreadB - subscribed to xxxPubSub
        ScheduledFuture<?> threadBFuture = threadPool.scheduleAtFixedRate(()->{
            System.out.println(xxxPubSub.sub.getMsg());
        }, 0, Util.toPeriod(threadBFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadAFuture.cancel(false);
        threadBFuture.cancel(false);
        return true;
    }


    private boolean syncOnePubToManySub(int queueSize) {
        final MQPubSubPair<String> xxxPubSub =
                new MQPubSubPair<>("PubSubTests", "xxx", 10);
        long threadAFreqInHz = 1000;
        long threadBFreqInHz = 100;

        // Thread A - publishing to xxxPubSub
        ScheduledFuture<?> threadAFuture = threadPool.scheduleAtFixedRate(()->{
            xxxPubSub.pub.publish("Sync 1-m  thread A : " + System.currentTimeMillis());
        }, 0, Util.toPeriod(threadAFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        // ThreadB - subscribed to xxxPubSub
        ScheduledFuture<?> threadBFuture = threadPool.scheduleAtFixedRate(()->{
            System.out.println(xxxPubSub.getSub().getMsg());
        }, 0, Util.toPeriod(threadBFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        // ThreadC - subscribed to xxxPubSub
        ScheduledFuture<?> threadCFuture = threadPool.scheduleAtFixedRate(()->{
            System.out.println(xxxPubSub.getSub().getMsg());
        }, 0, Util.toPeriod(threadBFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadAFuture.cancel(false);
        threadBFuture.cancel(false);
        threadCFuture.cancel(false);
        return true;

    }


    private boolean syncManyPubToOneSub(int queueSize) {
        final MQPubSubPair<String> xxxPubSub =
                new MQPubSubPair<>("PubSubTests", "xxx", 10);
        long threadAFreqInHz = 1000;
        long threadBFreqInHz = 100;

        // Thread A - publishing to xxxPubSub
        ScheduledFuture<?> threadAFuture = threadPool.scheduleAtFixedRate(()->{
            xxxPubSub.pub.publish("Sync m-1  thread A : " + System.currentTimeMillis());
        }, 0, Util.toPeriod(threadAFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        // ThreadB - publishing to xxxPubSub
        ScheduledFuture<?> threadBFuture = threadPool.scheduleAtFixedRate(()->{
            xxxPubSub.pub.publish("Sync m-1  thread B : " + System.currentTimeMillis());
        }, 0, Util.toPeriod(threadBFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        // ThreadC - subscribed to xxxPubSub
        ScheduledFuture<?> threadCFuture = threadPool.scheduleAtFixedRate(()->{
            System.out.println(xxxPubSub.getSub().getMsg());
        }, 0, Util.toPeriod(threadBFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadAFuture.cancel(false);
        threadBFuture.cancel(false);
        threadCFuture.cancel(false);
        return true;
    }


    private boolean syncManyPubToManySub(int queueSize) {
        final MQPubSubPair<String> xxxPubSub =
                new MQPubSubPair<>("PubSubTests", "xxx", 10);
        long threadAFreqInHz = 1000;
        long threadBFreqInHz = 100;

        // Thread A - publishing to xxxPubSub
        ScheduledFuture<?> threadAFuture = threadPool.scheduleAtFixedRate(()->{
            xxxPubSub.pub.publish("Sync m-m  thread A : " + System.currentTimeMillis());
        }, 0, Util.toPeriod(threadAFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        // ThreadB - publishing to xxxPubSub
        ScheduledFuture<?> threadBFuture = threadPool.scheduleAtFixedRate(()->{
            xxxPubSub.pub.publish("Sync m-m  thread B : " + System.currentTimeMillis());
        }, 0, Util.toPeriod(threadBFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        // ThreadC - subscribed to xxxPubSub
        ScheduledFuture<?> threadCFuture = threadPool.scheduleAtFixedRate(()->{
            System.out.println(xxxPubSub.getSub().getMsg());
        }, 0, Util.toPeriod(threadBFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        // ThreadD - subscribed to xxxPubSub
        ScheduledFuture<?> threadDFuture = threadPool.scheduleAtFixedRate(()->{
            System.out.println(xxxPubSub.getSub().getMsg());
        }, 0, Util.toPeriod(threadBFreqInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadAFuture.cancel(false);
        threadBFuture.cancel(false);
        threadCFuture.cancel(false);
        threadDFuture.cancel(false);
        return true;
    }


}
