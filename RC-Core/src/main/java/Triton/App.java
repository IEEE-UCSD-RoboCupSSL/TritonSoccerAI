package Triton;

import Triton.Vision.*;
import Triton.DesignPattern.MsgChannel;
import Triton.Detection.*;

public class App {
    private static final String VISION_MULTICAST_ADDR = "224.5.23.3";
    private static final int VISION_PORT = 10020;

    public static void main(String args[]) {

        MsgChannel.getInstance();

        VisionConnection vision = new VisionConnection(VISION_MULTICAST_ADDR, VISION_PORT);

        new Thread(new DetectionManager()).start();
        new Thread(new PosSubscriber()).start();

        while (true) {
            vision.collectData();
        }
    }
}
