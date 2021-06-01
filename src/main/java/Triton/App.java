package Triton;

import Triton.Config.*;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.Config.OldConfigs.*;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.*;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.ManualTests.CoreTestRunner;
import Triton.Misc.ModulePubSubSystem.FieldPubSubPair;
import Triton.PeriphModules.Detection.DetectionModule;
import Triton.PeriphModules.Vision.GrSimVisionModule_OldProto;
import Triton.VirtualBot.VirtualBotFactory;
import Triton.VirtualBot.VirtualBotList;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static Triton.Config.GlobalVariblesAndConstants.GvcGeneral.TotalNumOfThreads;
import static Triton.ManualTests.PeriphMiscTestRunner.runPeriphMiscTest;
import static Triton.Util.delay;


/**
 * Main Program
 * If wanting to manually compile & run:
 *  * install dependencies & plugins: mvn clean install
 *  * compile: mvn package
 *  * run:
 *      * mvn exec:java
 *      * or if with args: mvn exec:java -Dexec.args="arg1 arg2 ..."
 *  Example: mvn exec:java -Dexec.args="-bvt ini/Setups/DevelopmentSetups/virtual-grsim-6v6.ini ini/RobotConfig/triton-2021-grsim.ini"
 *
 * compile to a jar: mvn clean compile assembly:single
 * then use java -jar to execute the jar
 */
public class App {
    /* declare a global threadpool*/
    public static FieldPubSubPair<Boolean> appCanceller = new FieldPubSubPair<>("App", "Canceller", false);
    public static ScheduledExecutorService threadPool;
    static {
        /* Prepare a Thread Pool*/
        threadPool = new ScheduledThreadPoolExecutor(TotalNumOfThreads);
    }

    public static void main(String[] args) {
        /** process cli args & ini configs **/
        System.out.println("==============================================================");
        Config config = new Config(args);
        try {
            config.processAllConfigs();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(config.cliConfig);
        System.out.println(config.connConfig);
        // ...
        System.out.println("==============================================================");
        Scanner scanner = new Scanner(System.in);
        boolean toRunTest = false;
        boolean toTestTritonBot = false;
        ArrayList<ScheduledFuture<?>> virtualBotsFutures = null;

        if(config.cliConfig.isVirtualMode) {
            System.out.println("VirtualEnabled: waiting for TritonBot to connect to TritonSoccerAI's VirtualBots, then TritonSoccerAI will connect TritonBot on regular TCP & UDP ports");
            /* Note: VirtualBot has nothing to do with Robot(Ally/Foe), despite their naming similar.
             *      what VirtualBot really does is mocking the firmware layer of a real robot, whereas
             *      Robot(Ally/Foe) are internal OOP representation of a robot
            delay(1000);
             *  */
            VirtualBotList virtualBots = VirtualBotFactory.createVirtualBots(config);
            virtualBotsFutures = virtualBots.runAll();
            while(!virtualBots.areAllConnectedToTritonBots()) {
                delay(100);
            }
            delay(500);
        }
        
        GeometryConfig.initGeo(); // To-do: refactor this

        if(config.cliConfig.isTestMode) {
            System.out.println(">> Enter [Y] for CoreTest Mode, or Enter [N] for PeriphTest Mode");
            String testMode = scanner.nextLine();
            switch (testMode) {
                /* CoreTest Mode */
                case "Y" -> toRunTest = true;
                /* PeriphTest Mode */
                case "N" -> {
                    System.out.println("[PeriphTest Mode]: Testing for PeriphModules or misc staff");
                    runPeriphMiscTest(config);
                    toRunTest = true;
                }
                default -> System.out.println("Invalid Input");
            }
        }

        /* Instantiate & Run each independent modules in a separate thread from the thread threadPool */
        ScheduledFuture<?> visionFuture = App.threadPool.scheduleAtFixedRate(
                    new GrSimVisionModule_OldProto(config),
                0, Util.toPeriod(GvcModuleFreqs.GRSIM_VISION_MODULE_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        ScheduledFuture<?> detectFuture = App.threadPool.scheduleAtFixedRate(
                    new DetectionModule(config),
                0, Util.toPeriod(GvcModuleFreqs.DETECTION_MODULE_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        delay(1000);
        
        Ball ball = new Ball();
        ball.subscribe();

        // instantiate robots
        ArrayList<ScheduledFuture<?>> allyFieldersFutures = null;
        ArrayList<ScheduledFuture<?>> foesFutures = null;
        ScheduledFuture<?> goalKeeperFuture = null;
        RobotList<Ally> fielders = RobotFactory.createAllyFielderBots(config);
        Ally goalKeeper = RobotFactory.createGoalKeeperBot(config);
        RobotList<Foe> foes = RobotFactory.createFoeBotsForTracking(config);
        // our/ally robots == fielders + 1 goalkeeper
        if (fielders.connectAll() == config.connConfig.numRobots - 1 && goalKeeper.connect()) {
            allyFieldersFutures = fielders.runAll();
            goalKeeperFuture = App.threadPool.scheduleAtFixedRate(
                        goalKeeper,
                    0, Util.toPeriod(GvcModuleFreqs.ROBOT_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
        } else {
            System.out.println("Error Connecting to Robots (in App.java)");
        }
        // opponent/foe robots: foes = foeFielders + 1 foeGoalKeeper
        foesFutures = foes.runAll(); // submit all to threadPool


        if (toRunTest) {
            System.out.println("[CoreTest Mode]: Running TestRunner for testing CoreModules");
            CoreTestRunner.runCoreTest(config, fielders, goalKeeper, foes, ball);
        } else {
            System.out.println("Temporarily Down, Please Use Test Mode");
            /* Run the actual game program */
//
//            int port = (config.team == BLUE) ? 6543 : 6544;
//
//            GameCtrlModule gameCtrlModule = new PySocketGameCtrlModule(port);
////            GameCtrlModule gameCtrlModule = new SSLGameCtrlModule();
////            GameCtrlModule gameCtrlModule = new StdinGameCtrlModule(new Scanner(System.in));
//            ScheduledFuture<?> gameCtrlModuleFuture = App.threadPool.scheduleAtFixedRate(
//                    gameCtrlModule,
//                0, Util.toPeriod(GvcModuleFreqs.GAME_CTRL_MODULE_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
//
//            /* Instantiate & Run the main AI module, which is the core of this software */
//            threadPool.submit(new AI(config, fielders, goalKeeper, foes, ball, gameCtrlModule));
        }



        Util.sleepForever(appCanceller.sub);

        boolean toInterrupt = true;
        visionFuture.cancel(toInterrupt);
        detectFuture.cancel(toInterrupt);
        if(allyFieldersFutures != null) {
            for (ScheduledFuture<?> future : allyFieldersFutures) {
                future.cancel(toInterrupt);
            }
        }
        if(foesFutures != null) {
            for (ScheduledFuture<?> future : foesFutures) {
                future.cancel(toInterrupt);
            }
        }
        if(goalKeeperFuture != null) {
            goalKeeperFuture.cancel(toInterrupt);
        }

        if(virtualBotsFutures != null) {
            for (ScheduledFuture<?> future : virtualBotsFutures) {
                future.cancel(toInterrupt);
            }
        }

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


//        future.cancel(false);

    }
}
