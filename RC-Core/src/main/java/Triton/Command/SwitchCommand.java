package Triton.Command;

import Triton.Detection.DetectionPublisher;

public class SwitchCommand extends Command {

    public void execute() {
        if (!executed) {
            DetectionPublisher.toggle = !DetectionPublisher.toggle;
            System.out.print(DetectionPublisher.toggle ? "Fake" : "Real");
            System.out.println(" Detection Activated");
            executed = true;
        }
    }
}