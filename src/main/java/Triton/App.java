package Triton;

import Triton.Config.ObjectConfig;
import Triton.CoreModules.AI.AI;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.*;
import Triton.PeriphModules.Detection.DetectionModule;
import Triton.PeriphModules.Display.Display;
import Triton.PeriphModules.Display.PaintOption;
import Triton.PeriphModules.FieldGeometry.FieldGeometryModule;
import Triton.PeriphModules.Vision.GrSimVisionModule;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static Triton.Config.SimConfig.TOTAL_THREADS;
import static Triton.PeriphModules.Display.PaintOption.*;

/**
 * Main Program
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
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(TOTAL_THREADS, TOTAL_THREADS,
                0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());

        /* Instantiate & Run each independent modules in a separate thread from the thread threadPool */
        Runnable visionModule = new GrSimVisionModule();
        Runnable geoModule = new FieldGeometryModule();
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

        RobotList<Ally> allies = RobotFactory.createAllyBots(ObjectConfig.ROBOT_COUNT - 1, threadPool);
        Ally goalKeeper = RobotFactory.createGoalKeeperBot(threadPool);
        RobotList<Foe> foes = RobotFactory.createFoeBotsForTracking(ObjectConfig.ROBOT_COUNT, threadPool);
        if (allies.connectAll() == ObjectConfig.ROBOT_COUNT - 1
                && goalKeeper.connect()) {
            allies.runAll(threadPool);
            threadPool.submit(goalKeeper);
        }
//        allies.runAll(threadPool);
//        threadPool.submit(goalKeeper);
        foes.runAll(threadPool); // submit all to threadPool

        /* Instantiate & Run the main AI module, which is the core of this software */
        Runnable ai = new AI(allies, goalKeeper, foes, ball);
        threadPool.submit(ai);

        Display display = new Display();
        ArrayList<PaintOption> paintOptions = new ArrayList<>();
        paintOptions.add(GEOMETRY);
        paintOptions.add(OBJECTS);
        paintOptions.add(INFO);
        display.setPaintOptions(paintOptions);
    }
}
