package Triton.ManualTests.VirtualBotTests;

import Proto.FirmwareAPI;
import Triton.Config.Config;
import Triton.ManualTests.TritonTestable;
import Triton.ManualTests.VirtualBotTestRunner;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.SoccerObjects;

import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import static Triton.Util.delay;

public class VirtualMcuTopModuleTest implements TritonTestable {
    private final  SoccerObjects soccerObjects;
    // private FieldSubscriber<String> debugStrSub;

    public VirtualMcuTopModuleTest(SoccerObjects soccerObjects) {
        this.soccerObjects = soccerObjects;
    }

    @Override
    public boolean test(Config config) {
        Scanner scanner = new Scanner(System.in);
        int chosenBotId = 3;
        /* virtualbot modules have already been instantiate and run in App.java before calling this test */

        FieldSubscriber<FirmwareAPI.FirmwareCommand> cmdSub =
                new FieldSubscriber<>("[Pair]DefinedIn:VirtualBot", "FirmCmd " + chosenBotId);

        // debugStrSub = new FieldSubscriber<>("From:VirtualMcuTopModule", "DebugString " + chosenBotId);

        try {
            cmdSub.subscribe(1000);
            // debugStrSub.subscribe(1000);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        while (true) {
            System.out.println("Arbitrarily chosen bot's id: " + chosenBotId);
            VirtualBotTestRunner.sendPrimitiveCommand(scanner, soccerObjects.fielders.get(chosenBotId));

            delay(100);

            System.out.println("Command VirtualBot Received: >>>>>>>>>>");
            long t0 = System.currentTimeMillis();
            while(System.currentTimeMillis() - t0 < 1000) {
                System.out.println(cmdSub.getMsg());
            }
            //System.out.println(debugStrSub.getMsg());
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }
    }
}
