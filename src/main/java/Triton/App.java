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
import Triton.Misc.ModulePubSubSystem.Module;
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
 *
 *
 * Recommended Intellij config command line arg: "compile assembly:single"
 */
public class App {
    /* declare a global threadpool*/
    public static FieldPubSubPair<Boolean> appCanceller = new FieldPubSubPair<>("App", "Canceller", false);
    public static ScheduledExecutorService threadPool;
    public static ArrayList<ScheduledFuture<?>> moduleFutures = new ArrayList<>();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(config.cliConfig);
        System.out.println(config.connConfig);
        // ...
        System.out.println("==============================================================");
        Scanner scanner = new Scanner(System.in);
        if(config.cliConfig.isVirtualMode) {
            System.out.println("VirtualEnabled: waiting for TritonBot to connect to TritonSoccerAI's VirtualBots, then TritonSoccerAI will connect TritonBot on regular TCP & UDP ports");
            /* Note: VirtualBot has nothing to do with Robot(Ally/Foe), despite their naming similar.
             *      what VirtualBot really does is mocking the firmware layer of a real robot, whereas
             *      Robot(Ally/Foe) are internal OOP representation of a robot
            delay(1000);
             *  */
            VirtualBotList virtualBots = VirtualBotFactory.createVirtualBots(config);
            moduleFutures.addAll(virtualBots.runAll());
            while(!virtualBots.areAllConnectedToTritonBots()) {
                delay(100);
            }
            delay(1000);
        }
        
        GeometryConfig.initGeo(); // To-do: refactor this

        if(config.cliConfig.isTestMode) {
            boolean toQuit = false;
            do {
                System.out.println(">> Enter [C] for CoreTest Mode, [P] for PeriphTest Mode, [V] for VirtualBotTest Mode, or [quit] to exit");
                String testMode = scanner.nextLine();
                switch (testMode) {
                    case "quit" -> toQuit = true;
                    /* PeriphTest Mode */
                    case "p", "P" -> {
                        System.out.println("[PeriphTest Mode]: Testing for PeriphModules or misc staff");
                        runPeriphMiscTest(config, scanner);
                    }
                    /* CoreTest Mode */
                    case "c", "C" -> {
                        System.out.println("[CoreTest Mode]: Testing for CoreModules");

                        /* Instantiate & Run each independent module in a separate thread from the thread threadPool */
                        moduleFutures.add(App.runModule(new GrSimVisionModule_OldProto(config), GvcModuleFreqs.GRSIM_VISION_MODULE_FREQ));
                        moduleFutures.add(App.runModule(new DetectionModule(config), GvcModuleFreqs.DETECTION_MODULE_FREQ));
                        delay(1000);
                        SoccerObjects soccerObjects = new SoccerObjects(config);
                        moduleFutures.addAll(soccerObjects.runModules());

                        System.out.println("[CoreTest Mode]: Running TestRunner for testing CoreModules");
                        CoreTestRunner.runCoreTest(config, soccerObjects, scanner);
                    }
                    case "v", "V" -> {
                        if (config.cliConfig.isVirtualMode) {
                            System.out.println("[VirtualBotTest Mode]: Testing VirtualBotModules(a.k.a the virtual firmware modules)");

                            /* Instantiate & Run each independent module in a separate thread from the thread threadPool */
                            moduleFutures.add(App.runModule(new GrSimVisionModule_OldProto(config), GvcModuleFreqs.GRSIM_VISION_MODULE_FREQ));
                            moduleFutures.add(App.runModule(new DetectionModule(config), GvcModuleFreqs.DETECTION_MODULE_FREQ));
                            delay(1000);
                            SoccerObjects soccerObjects = new SoccerObjects(config);
                            moduleFutures.addAll(soccerObjects.runModules());


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
        } else {
            /* Normally Running a Game Mode */

            /* Instantiate & Run each independent module in a separate thread from the thread threadPool */
            moduleFutures.add(App.runModule(new GrSimVisionModule_OldProto(config), GvcModuleFreqs.GRSIM_VISION_MODULE_FREQ));
            moduleFutures.add(App.runModule(new DetectionModule(config), GvcModuleFreqs.DETECTION_MODULE_FREQ));
            delay(1000);
            SoccerObjects soccerObjects = new SoccerObjects(config);
            moduleFutures.addAll(soccerObjects.runModules());

            System.out.println("Temporarily suspended, Please Use Test Mode for now");
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



            Util.sleepForever(appCanceller.sub);
        }


//        for(ScheduledFuture<?> future : moduleFutures) {
//            future.cancel(true);
//        }



// OLD CODE:
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
