package Triton.ManualTests.VirtualBotTests;

import Proto.FirmwareAPI;
import Triton.Config.Config;
import Triton.ManualTests.TritonTestable;
import Triton.ManualTests.VirtualBotTestRunner;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.SoccerObjects;

import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import static Triton.Util.delay;

public class VirtualBotModuleTest implements TritonTestable {
    private final SoccerObjects soccerObjects;
    // private FieldSubscriber<String> debugStrSub;

    public VirtualBotModuleTest(SoccerObjects soccerObjects) {
        this.soccerObjects = soccerObjects;
    }

    @Override
    public boolean test(Config config) {
        Scanner scanner = new Scanner(System.in);
        int chosenBotId = 3;
        /* virtualbot modules have already been instantiate and run in App.java before calling this test */

        while (true) {
            System.out.println("Arbitrarily chosen bot's id: " + chosenBotId);
            VirtualBotTestRunner.sendPrimitiveCommand(scanner, soccerObjects.fielders.get(chosenBotId));
        }
    }
}
