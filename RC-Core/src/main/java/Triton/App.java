package Triton;

import java.util.ArrayList;
import java.util.concurrent.*;

import Triton.Vision.*;
import Triton.Computation.PathFinder.MoveTowardBall;
import Triton.Config.ObjectConfig;
import Triton.Detection.*;
import Triton.Geometry.*;
import Triton.RemoteStation.*;
import Triton.Display.*;

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

    private static int MAX_THREADS = 100;

    public static void main(String args[]) {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(MAX_THREADS, MAX_THREADS, 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());

        Runnable visionRunnable = new VisionModule();
        Runnable geoRunnable = new GeometryModule();
        Runnable detectRunnable = new DetectionModule();

        pool.submit(visionRunnable);
        pool.submit(geoRunnable);
        pool.submit(detectRunnable);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<Robot> robots = new ArrayList<Robot>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            Robot robot = new Robot(Team.YELLOW, i, pool);
            robots.add(robot);
            pool.submit(robot);
        }
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            Robot robot = new Robot(Team.BLUE, i, pool);
            robots.add(robot);
            pool.submit(robot);
        }

        Runnable moveTowardBallRunnable = new MoveTowardBall(robots.get(6));
        pool.submit(moveTowardBallRunnable);

        Display display = new Display();

        //robots.get(6).getRobotConnection().getTCPConnection().connect();
        //robots.get(6).getRobotConnection().getTCPConnection().sendInit();

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
