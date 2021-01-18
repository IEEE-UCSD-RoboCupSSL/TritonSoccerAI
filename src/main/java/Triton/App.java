package Triton;

import Triton.AI.AI;
import Triton.Config.ObjectConfig;
import Triton.Dependencies.Team;
import Triton.MovingObjectModules.Robot.Ally;
import Triton.MovingObjectModules.Robot.Foe;
import Triton.StandAlongModules.Detection.DetectionModule;
import Triton.StandAlongModules.Display.Display;
import Triton.StandAlongModules.Geometry.GeometryModule;
import Triton.StandAlongModules.Vision.GrSimVisionModule;
import Triton.MovingObjectModules.Ball.Ball;


import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Program to receive data from grSim and manage high-level behavior of robots
 */
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

    private final static int MAX_THREADS = 100;

    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            switch (args[0]) {
                case "BLUE" -> ObjectConfig.MY_TEAM = Team.BLUE;
                case "YELLOW" -> ObjectConfig.MY_TEAM = Team.YELLOW;
                default -> {
                    System.out.println("Error: Invalid Team");
                    return;
                }
            }
        }

        /* Prepare a Thread Pool*/
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(MAX_THREADS, MAX_THREADS, 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());

        /* Instantiate & Run each independent modules in a separate thread from the thread threadPool */
        Runnable visionModule = new GrSimVisionModule();
        Runnable geoModule = new GeometryModule();
        Runnable detectModule = new DetectionModule();
        threadPool.submit(visionModule);
        threadPool.submit(geoModule);
        threadPool.submit(detectModule);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* Instantiate & run Ball module*/
        Ball ball = new Ball();
        threadPool.submit(ball);

        /* Instantiate & run Our Robots (Ally) modules */
        ArrayList<Ally> allies = new ArrayList<Ally>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT - 1; i++) {
            Ally ally = new Ally(ObjectConfig.MY_TEAM, i, threadPool);
            allies.add(ally);
            threadPool.submit(ally);
        }
        Ally goalKeeper = new Ally(ObjectConfig.MY_TEAM, ObjectConfig.ROBOT_COUNT - 1, threadPool);
        threadPool.submit(goalKeeper);


        /* Instantiate & run Opponent Robots (Foe) modules */
        ArrayList<Foe> foes = new ArrayList<Foe>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            Team foeTeam = (ObjectConfig.MY_TEAM == Team.BLUE) ? Team.YELLOW : Team.BLUE;
            Foe foe = new Foe(foeTeam, i);
            foes.add(foe);
            threadPool.submit(foe);
        }

        /* Instantiate & Run the main AI module, which is the core of this software */
        Runnable ai = new AI(allies, goalKeeper, foes, ball);
        threadPool.submit(ai);

        /* A visualization tool */
        Display display = new Display();
    }
}
