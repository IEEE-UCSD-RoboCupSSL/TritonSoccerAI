package Triton.Command;

import Triton.Detection.DetectionData;

public class SwitchCommand extends Command {

    private Thread realThread;
    private Thread fakeThread;

    public SwitchCommand(Thread realThread, Thread fakeThread) {
        this.realThread = realThread;
        this.fakeThread = fakeThread;
    }

    public void execute() {
        if (!executed) {
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