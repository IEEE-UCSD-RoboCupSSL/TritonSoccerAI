package Triton;

import java.util.concurrent.*;
<<<<<<< HEAD
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashMap;
=======
>>>>>>> origin/pubsubsystem

import Triton.Vision.*;
import Triton.Detection.*;
import Triton.Geometry.*;
<<<<<<< HEAD
import Triton.RemoteStation.*;
=======
import Triton.RemoteStation.RobotConnetion;
import Triton.RemoteStation.RobotTCPConnection;
>>>>>>> origin/pubsubsystem
import Triton.Display.*;
import Triton.Command.*;

/*import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletHandler;*/

public class App {

    // TCP connection: listener, each robot connects to the listener, and the
    // server keeps the robot's port information [for further udp command sending]
    // and then the server send each robot the same geometry data through TCP
    // we should write a geometry protobuf

    // Multicast connection: broadcaster, use the data from the vision connection,
    // broadcast it
    // in our own vision protobuf format (processed vision)

    // UDP connection: sender, we have the robot port info when the tcp connection
    // is established

    // Each robot: listen high-level command on a port, send UDP EKF data to the
    // same port
    // Server: listen UDP EFK data on a port, host a multicast Vision port, listen
    // TCP on a port
    // send high-level command to one of 12 ports

    // 12(robot udp command listener) + 1(multicast vision) + 1(server udp ekf data
    // listener)
    // + 1(server tcp connection listener)

<<<<<<< HEAD
    public static void main(String args[]) {
        new Thread(new VisionConnection()).start();
        new Thread(new GeometryPublisher()).start();
        new Thread(new CommandPublisher()).start();

        //SwitchCommand.realThread = new Thread(new DetectionPublisher());
        //SwitchCommand.realThread.start();
        Lock detectionLock = new ReentrantLock();
        new Thread(new DetectionPublisher(detectionLock)).start();
        new Thread(new FakeDetectionPublisher(detectionLock)).start();
=======
    private static int MAX_THREADS = 100;

    public static void main(String args[]) {
        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);
>>>>>>> origin/pubsubsystem

        Runnable visionRunnable = new VisionModule();
        Runnable geoRunnable = new GeometryModule();
        Runnable detectRunnable = new DetectionModule();

<<<<<<< HEAD



        /*SwitchCommand.fakeThread = new Thread(new FakeDetectionPublisher());
        SwitchCommand.fakeThread.start();
        try {
            SwitchCommand.fakeThread.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        /*try {
            SwitchCommand.fakeThread.wait();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
=======
        pool.execute(visionRunnable);
        pool.execute(geoRunnable);
        pool.execute(detectRunnable);
>>>>>>> origin/pubsubsystem

        Display display = new Display();

        RobotConnetion robotConnect = new RobotConnetion(Team.YELLOW, 1, pool);
        robotConnect.buildTcpConnection("localhost", 6666);
        RobotTCPConnection tcpConn = robotConnect.getRobotTCPConnection();
        if(tcpConn.connect()) {
            System.out.println("Connected");
        }
        System.out.println(tcpConn.sendGeometry());

        /*ViewerServlet.offline = true;
        Server server = createServer(8980);
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    /*
    public static Server createServer(int port)
    {
        Server server = new Server(port);
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(ViewerServlet.class, "/ViewerServlet");

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler.setResourceBase(".");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, servletHandler });
        server.setHandler(handlers);

        return server;
    }
    */
}
