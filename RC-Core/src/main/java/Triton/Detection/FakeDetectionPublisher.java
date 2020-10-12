package Triton.Detection;

import java.util.concurrent.locks.Lock;

import Triton.Command.CommandData;
import Triton.Config.SimConfig;

public class FakeDetectionPublisher extends DetectionPublisher {
    
    public FakeDetectionPublisher(Lock detectionLock) {
        super(detectionLock);
    }

    public void run() {
        detectionLock.lock();
        while(true) {
            toggle();
            CommandData commands = CommandData.get();
            commands.executeAll();

            try {
                Thread.sleep(SimConfig.EXEC_INTERVAL);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }

    public void toggle() {
        if (!toggle) {
            detectionLock.unlock();
            detectionLock.lock();
        }
    }
}
