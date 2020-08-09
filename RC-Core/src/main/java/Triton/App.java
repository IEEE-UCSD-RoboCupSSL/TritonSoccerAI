package Triton;

import Triton.Vision.*;
import Triton.Detection.*;
import Triton.Geometry.*;
import Triton.RemoteStation.*;
import Triton.Display.*;

import java.util.HashMap;
import java.util.concurrent.*;

public class App {

    // TCP connection: listener, each robot connects to the listener, and the 
    // server keeps the robot's port information [for further udp command sending]
    // and then the server send each robot the same geometry data through TCP
    // we should write a geometry protobuf

    // Multicast connection: broadcaster, use the data from the vision connection, broadcast it 
    // in our own vision protobuf format (processed vision)

    // UDP connection: sender, we have the robot port info when the tcp connection is established

    // Each robot: listen high-level command on a port, send UDP EKF data to the same port
    // Server: listen UDP EFK data on a port, host a multicast Vision port, listen TCP on a port
    // send high-level command to one of 12 ports

    // 12(robot udp command listener) + 1(multicast vision) + 1(server udp ekf data listener)
    // + 1(server tcp connection listener)

    public static void main(String args[]) {
        new Thread(new VisionConnection("224.5.23.3", 10020)).start();
        new Thread(new DetectionPublisher()).start();
        new Thread(new GeometryPublisher()).start();
        //new Thread(new PosSubscriber()).start();
        //new Thread(new VelSubscriber()).start();
        //new Thread(new RegionSubscriber()).start();

        //new Thread(new MCVision()).start();

        /*FutureTask<HashMap<Boolean, HashMap<Integer, Integer>>> tcpTask = 
            new FutureTask<>(new TCPInit());
        new Thread(tcpTask).start();

        try {
            HashMap<Boolean, HashMap<Integer, Integer>> map = tcpTask.get();
            new Thread(new UDPSend(map)).start();
        } catch(Exception e) {
            e.printStackTrace();
        }*/

        new Thread(new Display()).start();
    }
}
