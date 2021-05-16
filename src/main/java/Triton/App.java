package Triton;

import Triton.Config.*;
import Triton.CoreModules.AI.AI;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.*;
import Triton.ManualTests.MiscTests.FutureTaskTest;
import Triton.ManualTests.MiscTests.PubSubTests;
import Triton.ManualTests.TestRunner;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.PeriphModules.Detection.DetectionModule;
import Triton.PeriphModules.GameControl.GameCtrlModule;
import Triton.PeriphModules.GameControl.PySocketGameCtrlModule;
import Triton.PeriphModules.Vision.GrSimVisionModule;

import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.*;

import static Triton.Config.ObjectConfig.MY_TEAM;
import static Triton.Config.ObjectConfig.ROBOT_COUNT;
import static Triton.Config.ThreadConfig.TOTAL_THREADS;
import static Triton.CoreModules.Robot.Team.BLUE;


/**
 * Main Program
 */
public class App {

    /* declare a global threadpool*/
    public static ScheduledExecutorService threadPool;


    static {
        /* Prepare a Thread Pool*/
        /*
        threadPool = new ThreadPoolExecutor(TOTAL_THREADS, TOTAL_THREADS, 0,
                TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>()) {
        };*/

        threadPool = new ScheduledThreadPoolExecutor(TOTAL_THREADS);
    }

    public static void main(String[] args) {
        boolean toRunTest = false;
        Scanner scanner = new Scanner(System.in);

        ConnectionProperties conn = Config.conn();

        /* processing command line arguments */
        if (args != null && args.length >= 1) { // choose team
            switch (args[0]) {
                case "BLUE" -> MY_TEAM = BLUE;
                case "YELLOW" -> MY_TEAM = Team.YELLOW;
                default -> {
                    System.out.println("Error: Invalid Team");
                    sleepForever();
                }
            }

            if (args.length >= 2) { // mode
                switch (args[1]) {
                    case "NORMAL" -> System.out.println("###########################################");
                    case "TEST" -> {

                        System.out.println(">> Enter [Y] for CoreTest Mode, or Enter [N] for PeriphTest Mode");
                        String testMode = scanner.nextLine();
                        switch (testMode) {
                            /* CoreTest Mode */
                            case "Y" -> toRunTest = true;

                            /* PeriphTest Mode */
                            case "N" -> {
                                System.out.println("[PeriphTest Mode]: Testing for PeriphModules or misc staff");
                                runPeriphMiscTest(scanner);
                                toRunTest = true;
                            }
                            default -> System.out.println("Invalid Input");
                        }

                    }
                    default -> {
                        System.out.println("Error: Invalid Args");
                        sleepForever();
                    }
                }
            }

            if (args.length >= 3) { // robot ip addr (port base)
                LinkedList<RobotIp> robotIPs = new LinkedList<>();
                for (int i = 0; i < ROBOT_COUNT; i++) { // use default port base and offset
                    int port = (args.length > 3) ? Integer.parseInt(args[3]) : conn.getDefaultPortBase();
                    port += i * conn.getDefaultPortOffset();
                    robotIPs.add(new RobotIp(args[2], port));
                }
                conn.setRobotIp(robotIPs);
            }
        }

        GeometryConfig.initGeo();

        /* Instantiate & Run each independent modules in a separate thread from the thread threadPool */
        Module visionModule = new GrSimVisionModule();
        Module detectModule = new DetectionModule();
        threadPool.submit(visionModule);
        threadPool.submit(detectModule);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* Instantiate & run Ball module*/
        Ball ball = new Ball();
        threadPool.submit(ball);

        RobotList<Ally> allies = RobotFactory.createAllyBots(ObjectConfig.ROBOT_COUNT - 1);
        Ally goalKeeper = RobotFactory.createGoalKeeperBot();
        RobotList<Foe> foes = RobotFactory.createFoeBotsForTracking(ObjectConfig.ROBOT_COUNT);
        if (allies.connectAll() == ObjectConfig.ROBOT_COUNT - 1
                && goalKeeper.connect()) {
            allies.runAll();
            threadPool.submit(goalKeeper);
        }
        foes.runAll(); // submit all to threadPool


        if (toRunTest) {
            System.out.println("[CoreTest Mode]: Running TestRunner for testing CoreModules");
            threadPool.submit(new TestRunner(scanner, allies, goalKeeper, foes, ball));
        } else {
            /* Run the actual game program */

            int port = (MY_TEAM == BLUE) ? 6543 : 6544;

            GameCtrlModule gameCtrlModule = new PySocketGameCtrlModule(port);
//            GameCtrlModule gameCtrlModule = new SSLGameCtrlModule();
//            GameCtrlModule gameCtrlModule = new StdinGameCtrlModule(new Scanner(System.in));
            threadPool.submit(gameCtrlModule);

            /* Instantiate & Run the main AI module, which is the core of this software */
            threadPool.submit(new AI(allies, goalKeeper, foes, ball, gameCtrlModule));
        }


//        Display display = new Display();
//        ArrayList<PaintOption> paintOptions = new ArrayList<>();
//        paintOptions.add(GEOMETRY);
//        paintOptions.add(OBJECTS);
//        paintOptions.add(INFO);
//        paintOptions.add(PROBABILITY);
//        paintOptions.add(PREDICTION);
//        display.setPaintOptions(paintOptions);

        sleepForever();
    }

    private static void sleepForever() {
        while (true) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void runPeriphMiscTest(Scanner scanner) {
        boolean quit = false;
        String prevTestName = "";
        while (!quit) {
            System.out.println(">> ENTER TEST NAME:");
            String testName = scanner.nextLine();
            boolean rtn = false;
            int repeat = 0;
            do {
                switch (testName) {
                    case "sayhi" -> {
                        System.out.println("Hi!");
                        rtn = true;
                    }

                    case "futask" -> rtn = new FutureTaskTest(threadPool).test();

                    case "pubsub" -> rtn = new PubSubTests(threadPool, scanner).test();

                    case "quit" -> {
                        quit = true;
                        rtn = true;
                    }
                    case "" -> {
                        repeat++;
                        testName = prevTestName;
                    }
                    default -> System.out.println("Invalid Test Name");
                }
            } while (repeat-- > 0);
            repeat = 0;
            prevTestName = testName;
            if(!quit) System.out.println(rtn ? "Test Success" : "Test Fail");
        }

        System.out.println("PeriphTest Ended");
        System.out.println("Automatically run CoreTest TestRunner next\n");
    }

}


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