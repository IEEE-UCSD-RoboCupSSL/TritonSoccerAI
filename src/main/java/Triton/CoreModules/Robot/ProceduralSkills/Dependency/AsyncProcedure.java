package Triton.CoreModules.Robot.ProceduralSkills.Dependency;

import Triton.CoreModules.Robot.Ally.Ally;

import java.util.concurrent.*;

public class AsyncProcedure {
    final private ExecutorService threadPool;
    private FutureTask<Boolean> futureTask = null;
    ProceduralTask task = null;

    public AsyncProcedure(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    /* execute the task or do nothing if already it has already been running */
    public void execute(Ally thisRobot, ProceduralTask task) {
        if(futureTask == null || futureTask.isDone()) {
            this.task = task;
            this.task.sendCancelSignal(false);
            task.registerThisRobot(thisRobot);
            futureTask = new FutureTask<>(this.task);
            threadPool.submit(futureTask);
        }
    }

    public void reset() {
        futureTask = null;
        task = null;
    }

    public boolean isCompleted() {
        if(futureTask == null) return false;
        return futureTask.isDone();
        //return futureTask.isDone() && !futureTask.isCancelled();
    }

    public boolean getCompletionReturn() throws ExecutionException, InterruptedException {
        if(futureTask == null) return false;
        return futureTask.get();
    }


    public boolean isCancelled() {
        if(task == null) return false;
        //return futureTask.isCancelled();
        return task.isCancelled();
    }


    public void cancel() {
        if(task != null) {
            task.sendCancelSignal(true);
            /* don't use FutureTask's built-in cancel, or else strange bug occurs */ //futureTask.cancel(false);
        }
    }

}
