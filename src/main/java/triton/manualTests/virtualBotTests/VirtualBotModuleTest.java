package triton.manualTests.virtualBotTests;

import triton.config.Config;
import triton.manualTests.TritonTestable;
import triton.misc.modulePubSubSystem.FieldSubscriber;
import triton.misc.modulePubSubSystem.Subscriber;
import triton.SoccerObjects;
import triton.virtualBot.VirtualBotCmds;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class VirtualBotModuleTest implements TritonTestable {
    private final SoccerObjects soccerObjects;
    // private FieldSubscriber<String> debugStrSub;
    protected final ArrayList<Subscriber<VirtualBotCmds>> virtualBotCmdSubs = new ArrayList<>();


    public VirtualBotModuleTest(SoccerObjects soccerObjects) {
        this.soccerObjects = soccerObjects;
    }

    @Override
    public boolean test(Config config) {
        Scanner scanner = new Scanner(System.in);
        int chosenBotId = 3;
        /* virtualbot modules have already been instantiate and run in App.java before calling this test */

        for (int i = 0; i < config.numAllyRobots; i++) {
            virtualBotCmdSubs.add(new FieldSubscriber<>("From:VirtualBot", "Cmd " + i));
        }
        for (Subscriber<VirtualBotCmds> allyCmdSub : virtualBotCmdSubs) {
            try {
                allyCmdSub.subscribe(1000);
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }

        while (true) {
            System.out.println("Arbitrarily chosen bot's id: " + chosenBotId);
            VirtualBotTestRunner.sendPrimitiveCommand(scanner, soccerObjects.fielders.get(chosenBotId));

            long t0 = System.currentTimeMillis();
            while(System.currentTimeMillis() - t0 < 1000) {
                VirtualBotCmds cmd = virtualBotCmdSubs.get(chosenBotId).getMsg();
                System.out.println(cmd);
            }

        }
    }
}
