package Triton;

import Triton.Config.*;
import Triton.Config.GlobalVariblesAndConstants.GvcAI;
import Triton.Config.GlobalVariblesAndConstants.GvcGeneral;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.CoreModules.AI.AI;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.ManualTests.CoreTests.CoreTestRunner;
import Triton.ManualTests.CoreTests.RobotSkillsTests.PrimitiveMotionTest;
import Triton.ManualTests.VirtualBotTests.SimClientModuleTest;
import Triton.ManualTests.VirtualBotTests.VirtualBotTestRunner;
import Triton.Misc.ModulePubSubSystem.FieldPubSubPair;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.PeriphModules.Detection.DetectionModule;
import Triton.PeriphModules.GameControl.GameCtrlModule;
import Triton.PeriphModules.GameControl.SSLGameCtrlModule;
import Triton.PeriphModules.Vision.ERForceVisionModule;
import Triton.PeriphModules.Vision.GrSimVisionModule_OldProto;
import Triton.VirtualBot.*;
import Triton.VirtualBot.SimulatorDependent.ErForce.ErForceClientModule;
import Triton.VirtualBot.SimulatorDependent.GrSim_OldProto.GrSimClientModule;
import Triton.Config.GlobalVariblesAndConstants.GvcGeneral.SimulatorName;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static Triton.Config.GlobalVariblesAndConstants.GvcGeneral.TotalNumOfThreads;
import static Triton.ManualTests.PeriphMiscTests.PeriphMiscTestRunner.runPeriphMiscTest;
import static Triton.Util.delay;


/**
 * Main Program
 * If wanting to manually compile & run:
 *  * install dependencies & plugins: mvn clean install
 *  * compile: mvn package
 *  * run:
 *      * mvn exec:java
 *      * or if with args: mvn exec:java -Dexec.args="arg1 arg2 ..."
 *  Example: mvn exec:java -Dexec.args="-bvm test"
 *
 * compile to a jar: mvn clean compile assembly:single
 * then use java -jar to execute the jar
 *
 *
 * Recommended Intellij config command line arg: "compile assembly:single"
 */
public class App {
    /* declare a global threadpool*/
    public static FieldPubSubPair<Boolean> appCanceller = new FieldPubSubPair<>("From:App", "Canceller", false);
    public static ScheduledExecutorService threadPool;
    public static ArrayList<ScheduledFuture<?>> moduleFutures = new ArrayList<>();
    public static GameCtrlModule gameCtrlModule = null;

    static {
        /* Prepare a Thread Pool*/
        threadPool = new ScheduledThreadPoolExecutor(TotalNumOfThreads);
    }

    public static ScheduledFuture<?> runModule(Module module, double frequencyInHz) {
         return App.threadPool.scheduleAtFixedRate(
                module,
            0, Util.toPeriod(frequencyInHz, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
    }

    public static void main(String[] args) {
        /** process cli args & ini configs **/
        System.out.println("==============================================================");
        Config config = new Config(args);
        try {
            config.processAllConfigs();
            GvcAI.globalConfig_AdHoc = config;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(config);
        // ...
        System.out.println("==============================================================");

        Scanner scanner = new Scanner(System.in);
        if(config.cliConfig.isVirtualSetup && config.cliConfig.progMode != GvcGeneral.ProgramMode.TestTritonBot) {
            setupInternalVirtualBots(config);
        }

        switch (config.cliConfig.progMode) {
            case Normal -> {
                /* Run the actual game program */
                /* Instantiate & Run each independent module in a separate thread from the thread threadPool */
                /* Instantiate & Run each independent module in a separate thread from the thread threadPool */
                createAndRunPeriphModules(config, true);
                delay(500);
                SoccerObjects soccerObjects = new SoccerObjects(config);
                moduleFutures.addAll(soccerObjects.runModules());
                delay(500);
//            /* Instantiate & Run the main AI module, which is the core of this software */
                App.runModule(new AI(config, soccerObjects, gameCtrlModule), GvcModuleFreqs.AI_MODULE_FREQ);
                Util.sleepForever(appCanceller.sub);
            }
            case Test -> {
                handleTestMode(config, scanner);
            }
            case TestTritonBot -> {
                handleTestTritonBotMode(config, scanner);
            }
        }

        // cancel not working yet, need extra mechanism,
        for(ScheduledFuture<?> future : moduleFutures) {
            future.cancel(true);
        }
    }

    public static void createAndRunPeriphModules(Config config, boolean runGameCtrl) {
        if(config.cliConfig.isVirtualSetup) {
            switch (config.cliConfig.simulator) {
                case GrSim -> {
                    moduleFutures.add(App.runModule(
                            new GrSimVisionModule_OldProto(config), GvcModuleFreqs.VISION_MODULE_FREQ)
                    );
                }
                case ErForceSim -> {
                    moduleFutures.add(App.runModule(
                            new ERForceVisionModule(config), GvcModuleFreqs.VISION_MODULE_FREQ)
                    );
                }
            }
        } else {
            System.err.println("Error: WorkInProgress");
        }
        moduleFutures.add(App.runModule(new DetectionModule(config), GvcModuleFreqs.DETECTION_MODULE_FREQ));
        if(runGameCtrl) {
            gameCtrlModule = new SSLGameCtrlModule(config);
            App.runModule(gameCtrlModule, GvcModuleFreqs.GAME_CTRL_MODULE_FREQ);
        }
        delay(1000);
    }

    public static void handleTestMode(Config config, Scanner scanner) {
        boolean toQuit = false;
        do {
            System.out.println(">> Enter [C] for CoreTest Mode, [P] for PeriphMiscTest Mode, [V] for VirtualBotTest Mode, or [quit] to exit");
            String testMode = scanner.nextLine();
            switch (testMode) {
                case "quit" -> toQuit = true;
                /* PeriphTest Mode */
                case "p", "P" -> {
                    System.out.println("[PeriphMiscTest Mode]: Testing for PeriphMiscModules or misc staff");
                    runPeriphMiscTest(config, scanner);
                }
                /* CoreTest Mode */
                case "c", "C" -> {
                    System.out.println("[CoreTest Mode]: Testing for CoreModules");

                    /* Instantiate & Run each independent module in a separate thread from the thread threadPool */
                    createAndRunPeriphModules(config, false);
                    delay(500);
                    SoccerObjects soccerObjects = new SoccerObjects(config);
                    moduleFutures.addAll(soccerObjects.runModules());
                    delay(500);


                    System.out.println("[CoreTest Mode]: Running TestRunner for testing CoreModules");
                    CoreTestRunner.runCoreTest(config, soccerObjects, scanner);
                }
                /* VirtualBot Test Mode */
                case "v", "V" -> {
                    if (config.cliConfig.isVirtualSetup) {
                        System.out.println("[VirtualBotTest Mode]: Testing VirtualBotModules(a.k.a the virtual firmware modules)");

                        /* Instantiate & Run each independent module in a separate thread from the thread threadPool */
                        createAndRunPeriphModules(config, false);
                        delay(500);
                        SoccerObjects soccerObjects = new SoccerObjects(config);
                        moduleFutures.addAll(soccerObjects.runModules());
                        delay(500);
                        VirtualBotTestRunner.runVirtualBotTest(config, scanner, soccerObjects);
                    } else {
                        System.err.println("Test V mode require this program to be running virtual mode");
                    }
                }
                default -> System.out.println("Invalid Input");
            }
        } while(!toQuit);
        System.exit(0);  // do expect some error message got printed out because both this & cpp part
        // haven't handle socket exceptions smartly which have them reconnect upon exception
        // occurring, use ctrl+c to exit
    }


    public static void handleTestTritonBotMode(Config config, Scanner scanner) {
        int id = 0;
        createAndRunPeriphModules(config, false);
        setupOneInternalVirtualBot(config, id);
        Ally bot;
        System.out.println("......");
        bot = new Ally(config, id);
        bot.connect();
        moduleFutures.add(
                App.runModule(bot, GvcModuleFreqs.ROBOT_FREQ)
        );
        delay(1500);

        System.out.println(">> Enter [pm] to run primitive motion, [ph] to print hold-ball, or [quit] to exit");
        String testMode = scanner.nextLine();
        switch (testMode) {
            case "pm" -> {
                new PrimitiveMotionTest(bot).test(config);
            }
            case "ph" -> {
                System.out.println("This test will quit in 5 seconds");
                long t0 = System.currentTimeMillis();
                while(System.currentTimeMillis() - t0 < 5000) {
                    System.out.println(bot.isHoldingBall());
                    delay(100);
                }
            }
            case "quit" -> System.exit(0);
            default -> {
                System.err.println("Unknown Mode");
            }
        }
    }

    public static void setupInternalVirtualBots(Config config) {
        System.out.println("\033[0;32m VirtualEnabled: waiting for TritonBot to connect to " +
                "TritonSoccerAI's VirtualBots, then TritonSoccerAI will connect " +
                "TritonBot on regular TCP & UDP ports \033[0m");

        SimClientModule simClientModule = null;
        if(config.cliConfig.simulator == SimulatorName.GrSim) {
            simClientModule = new GrSimClientModule(config);
        } else if(config.cliConfig.simulator == SimulatorName.ErForceSim) {
            simClientModule = new ErForceClientModule(config);
        }
        App.runModule(simClientModule, GvcModuleFreqs.SIM_CLIENT_FREQ);
        /* Note: VirtualBot has nothing to do with Robot(Ally/Foe), despite their naming similar.
         *      what VirtualBot really does is mocking the firmware layer of a real robot, whereas
         *      Robot(Ally/Foe) are internal OOP representation of a robot
         *  */
        VirtualBotList virtualBots = VirtualBotFactory.createVirtualBots(config);
        moduleFutures.addAll(virtualBots.runAll());
        while(!virtualBots.areAllConnectedToTritonBots()) {
            delay(100);
        }
        delay(1000);
    }

    private static void setupOneInternalVirtualBot(Config config, int id) {
        System.out.println("~~~~~~");

        /* just taking advantage of this tester's constructor, which constructs the publishers
           matching those subscribers inside simClientModule, preventing subscribe timeout exception */
        new SimClientModuleTest();

        SimClientModule simClientModule = null;
        if(config.cliConfig.simulator == GvcGeneral.SimulatorName.GrSim) {
            simClientModule = new GrSimClientModule(config);
        }
        App.runModule(simClientModule, GvcModuleFreqs.SIM_CLIENT_FREQ);
        /* Note: VirtualBot has nothing to do with Robot(Ally/Foe), despite their naming similar.
         *      what VirtualBot really does is mocking the firmware layer of a real robot, whereas
         *      Robot(Ally/Foe) are internal OOP representation of a robot
         *  */

        VirtualBot vbot = new VirtualBot(config, id);
        ScheduledFuture<?> robotFuture = App.threadPool.scheduleAtFixedRate(
                vbot,
                0, Util.toPeriod(GvcModuleFreqs.VIRTUAL_BOT_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
        delay(1000);
    }
}



//
//        Display display = new Display(config);
//        ArrayList<PaintOption> paintOptions = new ArrayList<>();
//        paintOptions.add(GEOMETRY);
//        paintOptions.add(OBJECTS);
//        paintOptions.add(INFO);
//        paintOptions.add(PROBABILITY);
//        paintOptions.add(PREDICTION);
//        paintOptions.add(DRAWABLES);
//        display.setPaintOptions(paintOptions);
//
//        ScheduledFuture<?> displayFuture = App.threadPool.scheduleAtFixedRate(display,
//                0,
//                Util.toPeriod(GvcModuleFreqs.DISPLAY_MODULE_FREQ, TimeUnit.NANOSECONDS),
//                TimeUnit.NANOSECONDS);

//        Publisher<ArrayList<Drawable2D>> drawablePub = new FieldPublisher<>("[Pair]DefinedIn:Display", "Drawables", new ArrayList<>());
//        ArrayList<Drawable2D> drawables = new ArrayList<>();
//        Circle2D circle = new Circle2D(new Vec2D(0, 0), 100);
//        drawables.add(circle);
//        Line2D line = new Line2D(new Vec2D(0, 0), new Vec2D(500, 500));
//        drawables.add(line);
//        Rect2D rect = new Rect2D(new Vec2D(100, 100), 200, 200);
//        drawables.add(rect);
//        drawablePub.publish(drawables);