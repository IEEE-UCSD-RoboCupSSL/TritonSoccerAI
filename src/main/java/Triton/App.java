package Triton;

import Triton.Config.*;
import Triton.Config.OldConfigs.*;
import Triton.CoreModules.AI.AI;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.*;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.ManualTests.CoreTestRunner;
import Triton.ManualTests.RobotSkillsTests.PrimitiveMotionTest;
import Triton.ManualTests.TritonTestable;
import Triton.PeriphModules.Detection.DetectionModule;
import Triton.PeriphModules.GameControl.GameCtrlModule;
import Triton.PeriphModules.GameControl.PySocketGameCtrlModule;
import Triton.PeriphModules.Vision.GrSimVisionModule;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static Triton.Config.OldConfigs.DEPRECATED_ConnectionConfig.*;
import static Triton.Config.OldConfigs.ObjectConfig.MY_TEAM;
import static Triton.Config.OldConfigs.ObjectConfig.ROBOT_COUNT;
import static Triton.Config.OldConfigs.ThreadConfig.TOTAL_THREADS;
import static Triton.CoreModules.Robot.Team.BLUE;
import static Triton.ManualTests.PeriphMiscTestRunner.runPeriphMiscTest;


/**
 * Main Program
 * If wanting to manually compile & run:
 *  * install dependencies & plugins: mvn clean install
 *  * compile: mvn package
 *  * run:
 *      * mvn exec:java
 *      * or if with args: mvn exec:java -Dexec.args="arg1 arg2 ..."
 *  Example: mvn exec:java -Dexec.args="-b -v ini/Setups/DevelopmentSetups/virtual-grsim-6v6.ini ini/RobotConfig/triton-2021-grsim.ini"
 *
 * compile to a jar: mvn clean compile assembly:single
 * then use java -jar to execute the jar
 */
public class App {
    /* declare a global threadpool*/
    public static ScheduledExecutorService threadPool;
    static {
        /* Prepare a Thread Pool*/
        threadPool = new ScheduledThreadPoolExecutor(TOTAL_THREADS);
    }

    public static void main(String[] args) {
        System.out.println("==============================================================");
        Config config = new Config(args);
        try {
            config.processAllConfigs();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(config.cliConfig);
        System.out.println(config.connConfig);
        System.out.println("==============================================================");

        sleepForever();

        boolean toRunTest = false;
        boolean toTestTritonBot = false;
        Scanner scanner = new Scanner(System.in);

        ConnectionProperties conn = jsonConfig.conn();

        IniFileProcessor.readIni();

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
                                runPeriphMiscTest();
                                toRunTest = true;
                            }
                            default -> System.out.println("Invalid Input");
                        }
                    }
                    case "TEST_TRITONBOT" -> {
                        toTestTritonBot = true;
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
            if (args.length >= 4) {
                switch (args[3]) {
                    case "GRSIM" -> {
                        SystemConfig.SIM = Simulator.GRSIM;
                    }
                    case "ERFORCE" -> {
                        SystemConfig.SIM = Simulator.ERFORCE;
                    }
                }
            }
        }

        GeometryConfig.initGeo();

        if(toTestTritonBot) {
            testTritonBotMode(scanner);
        }

        /* Instantiate & Run each independent modules in a separate thread from the thread threadPool */
        ScheduledFuture<?> visionFuture = App.threadPool.scheduleAtFixedRate(new GrSimVisionModule(),
                0, Util.toPeriod(ModuleFreqConfig.GRSIM_VISION_MODULE_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        ScheduledFuture<?> detectFuture = App.threadPool.scheduleAtFixedRate(new DetectionModule(),
                0, Util.toPeriod(ModuleFreqConfig.DETECTION_MODULE_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Ball ball = new Ball();
        ball.subscribe();

        RobotList<Ally> fielders = RobotFactory.createAllyBots(ObjectConfig.ROBOT_COUNT - 1);
        Ally goalKeeper = RobotFactory.createGoalKeeperBot();
        RobotList<Foe> foes = RobotFactory.createFoeBotsForTracking(ObjectConfig.ROBOT_COUNT);
        if (fielders.connectAll() == ObjectConfig.ROBOT_COUNT - 1
                && goalKeeper.connect()) {
            fielders.runAll();

            ScheduledFuture<?> goalKeeperFuture = App.threadPool.scheduleAtFixedRate(goalKeeper,
                    0, Util.toPeriod(ModuleFreqConfig.ROBOT_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
        }
        foes.runAll(); // submit all to threadPool


        if (toRunTest) {
            System.out.println("[CoreTest Mode]: Running TestRunner for testing CoreModules");
            CoreTestRunner.runCoreTest(fielders, goalKeeper, foes, ball);
        } else {
            /* Run the actual game program */

            int port = (MY_TEAM == BLUE) ? 6543 : 6544;

            GameCtrlModule gameCtrlModule = new PySocketGameCtrlModule(port);
//            GameCtrlModule gameCtrlModule = new SSLGameCtrlModule();
//            GameCtrlModule gameCtrlModule = new StdinGameCtrlModule(new Scanner(System.in));
            ScheduledFuture<?> gameCtrlModuleFuture = App.threadPool.scheduleAtFixedRate(gameCtrlModule,
                    0, Util.toPeriod(ModuleFreqConfig.GAME_CTRL_MODULE_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

            /* Instantiate & Run the main AI module, which is the core of this software */
            threadPool.submit(new AI(fielders, goalKeeper, foes, ball, gameCtrlModule));
        }

        /*
        for (int i = 0; i < ROBOT_COUNT; i++) {
            ScheduledFuture<?> tritonBotReceiveModuleFuture = App.threadPool.scheduleAtFixedRate(
                    new TritonBotReceiveModule(TRITON_IP, TRITON_PORT, i),
                    0, Util.toPeriod(ModuleFreqConfig.TRITON_BOT_RECEIVE_FREQ, TimeUnit.NANOSECONDS),
                    TimeUnit.NANOSECONDS);
        }*/


        /*
        // Schedule VirtualBot modules
        switch (SystemConfig.SIM) {
            case GRSIM -> {
                ScheduledFuture<?> grSimProcessingFuture = App.threadPool.scheduleAtFixedRate(
                        new GrSimProcessingModule(),
                        0, Util.toPeriod(ModuleFreqConfig.GRSIM_PROCESSING_FREQ, TimeUnit.NANOSECONDS),
                        TimeUnit.NANOSECONDS);

                ScheduledFuture<?> grSimSendFuture = App.threadPool.scheduleAtFixedRate(
                        new GrSimSendModule(GRSIM_SEND_IP, GRSIM_SEND_PORT),
                        0, Util.toPeriod(ModuleFreqConfig.GRSIM_SEND_FREQ, TimeUnit.NANOSECONDS),
                        TimeUnit.NANOSECONDS);
            }
            case ERFORCE -> {
                ScheduledFuture<?> grSimProcessingFuture = App.threadPool.scheduleAtFixedRate(
                        new ErForceProcessingModule(),
                        0, Util.toPeriod(ModuleFreqConfig.ERFORCE_PROCESSING_FREQ, TimeUnit.NANOSECONDS),
                        TimeUnit.NANOSECONDS);

                ScheduledFuture<?> grSimSendFuture = App.threadPool.scheduleAtFixedRate(
                        new ErForceSendModule(GRSIM_SEND_IP, GRSIM_SEND_PORT),
                        0, Util.toPeriod(ModuleFreqConfig.ERFORCE_SEND_FREQ, TimeUnit.NANOSECONDS),
                        TimeUnit.NANOSECONDS);
            }
        }

         */

//        Display display = new Display();
//        ArrayList<PaintOption> paintOptions = new ArrayList<>();
//        paintOptions.add(GEOMETRY);
//        paintOptions.add(OBJECTS);
//        paintOptions.add(INFO);
//        paintOptions.add(PROBABILITY);
//        paintOptions.add(PREDICTION);
//        display.setPaintOptions(paintOptions);

//        ScheduledFuture<?> displayFuture = App.threadPool.scheduleAtFixedRate(display,
//                0,
//                Util.toPeriod(ModuleFreqConfig.DISPLAY_MODULE_FREQ, TimeUnit.NANOSECONDS),
//                TimeUnit.NANOSECONDS);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        future.cancel(false);

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

    private static void testTritonBotMode(Scanner scanner) {
        System.out.println("Test Started!");


        /* Instantiate & Run each independent modules in a separate thread from the thread threadPool */
        ScheduledFuture<?> visionFuture = App.threadPool.scheduleAtFixedRate(new GrSimVisionModule(),
                0, Util.toPeriod(ModuleFreqConfig.GRSIM_VISION_MODULE_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        ScheduledFuture<?> detectFuture = App.threadPool.scheduleAtFixedRate(new DetectionModule(),
                0, Util.toPeriod(ModuleFreqConfig.DETECTION_MODULE_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);


        Ball ball = new Ball();
        ball.subscribe();

        final Ally ally = new Ally(ObjectConfig.MY_TEAM, 0);
        ally.connect();

        App.threadPool.scheduleAtFixedRate(ally,
                0, Util.toPeriod(ModuleFreqConfig.ROBOT_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);


        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TreeMap<String, TritonTestable> testMap = new TreeMap<>();
        testMap.put("PrimitiveMotion", (new PrimitiveMotionTest(ally)));
        testMap.put("PrintHoldBall", new TritonTestable() {
            @Override
            public boolean test() {
                System.out.println("This test will quit in 5 seconds");
                long t0 = System.currentTimeMillis();
                while(System.currentTimeMillis() - t0 < 5000) {
                    System.out.println(ally.isHoldingBall());
                }
                return true;
            }
        });


        System.out.println("Available Tests:");
        for (String test : testMap.keySet()) {
            System.out.printf("- %s \n", test);
        }
        System.out.println();

        String prevTestName = "";
        while (true) {
            boolean result = false;
            System.out.println(">> ENTER TEST NAME:");
            String testName = scanner.nextLine();
            if (testName.equals("")) {
                testName = prevTestName;
            } else if (testName.equals("quit")) {
                break;
            }

            TritonTestable test = testMap.get(testName);
            Optional<TritonTestable> test1 = Optional.ofNullable(test);

            if (test1.isEmpty()) {
                System.out.println("Invalid Test Name");
                continue;
            } else {
                result = test1.get().test();
            }

            prevTestName = testName;
            System.out.println(result ? "Test Success" : "Test Fail");
        }
        //sleepForever();
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