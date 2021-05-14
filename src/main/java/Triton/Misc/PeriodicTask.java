package Triton.Misc;

import java.util.Timer;
import java.util.TimerTask;

public class PeriodicTask {

    private final Timer timer;
    public PeriodicTask(Timer timer) {
        this.timer = timer;
    }


    public TimerTask schedule(Runnable toRun, long periodInMillis) {
        return schedule(toRun, 0, periodInMillis);
    }

    public TimerTask schedule(Runnable toRun, long delayInMillis, long periodInMillis) {
        final TimerTask tTask = new TimerTask() {
            @Override
            public void run() {
                toRun.run();
            }
        };
        timer.scheduleAtFixedRate(tTask, delayInMillis, periodInMillis);
        return tTask;
    }




}
