package Triton;

import Triton.DesignPattern.MsgChannel;
import Triton.Vision.*;
import Triton.Detection.*;

public class App {

    public static void main(String args[]) {

        MsgChannel.getInstance(); // Initailize Msg Channel

        new Thread(new VisionConnection("224.5.23.3", 10020)).start();
        new Thread(new DetectionPublisher()).start();
        //new Thread(new PosSubscriber()).start();
        new Thread(new VelSubscriber()).start();
    }
}
