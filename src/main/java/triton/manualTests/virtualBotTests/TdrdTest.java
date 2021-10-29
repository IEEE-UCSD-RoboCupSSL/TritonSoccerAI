package triton.manualTests.virtualBotTests;

import proto.FirmwareAPI;
import triton.config.Config;
import triton.coreModules.robot.ally.Ally;
import triton.manualTests.TritonTestable;
import triton.misc.math.linearAlgebra.Vec2D;
import triton.misc.modulePubSubSystem.FieldSubscriber;
import triton.SoccerObjects;

import java.util.concurrent.TimeoutException;

import static triton.Util.delay;

public class TdrdTest implements TritonTestable {
    private final  SoccerObjects soccerObjects;
    // private FieldSubscriber<String> debugStrSub;

    public TdrdTest(SoccerObjects soccerObjects) {
        this.soccerObjects = soccerObjects;
    }

    @Override
    public boolean test(Config config) {
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

        System.out.println("Arbitrarily chosen bot's id: " + chosenBotId);

        Ally bot = soccerObjects.fielders.get(chosenBotId);
        bot.moveTo(new Vec2D(0, 0));
        bot.spinTo(45);
        while (true) {
            System.out.print("\033[H\033[2J");
            System.out.flush();
            System.out.println("Command VirtualBot Received: >>>>>>>>>>");
            System.out.println(cmdSub.getMsg());
            //System.out.println(debugStrSub.getMsg());
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            delay(300);
        }
    }
}
