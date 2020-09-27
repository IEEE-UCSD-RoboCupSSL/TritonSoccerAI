package Triton.Command;

import Triton.ThreadManager.ThreadManager;

public class SwitchCommand extends Command {

    private Thread realThread;
    private Thread fakeThread;

    public SwitchCommand() {
        ThreadManager threadManager = ThreadManager.getManager();
        realThread = threadManager.getThread("Detection");
        fakeThread = threadManager.getThread("FakeDetection");
    }

    public void execute() {
        if (!executed) {
            System.out.println("!!!");
            if (realThread.getState() == Thread.State.TIMED_WAITING) {
                SwitchCommand.world.transfer(DetectionData.get());
                try {
                    realThread.wait();
                    fakeThread.notify();
                } catch (Exception e) {
                }
            } else {
                try {
                    realThread.notify();
                    fakeThread.wait();
                } catch (Exception e) {
                }
            }
            executed = true;
        }
    }
}