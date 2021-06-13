package Triton.ManualTests.VirtualBotTests;

import Triton.Config.Config;
import Triton.ManualTests.TritonTestable;
import Triton.Misc.ModulePubSubSystem.FieldPublisher;
import Triton.Misc.ModulePubSubSystem.Publisher;
import Triton.VirtualBot.VirtualBotCmds;

import java.util.ArrayList;
import java.util.Scanner;

public class GrSimClientModuleTest implements TritonTestable {
    private final ArrayList<Publisher<VirtualBotCmds>> virtualBotCmdSubs = new ArrayList<>();

    public GrSimClientModuleTest(Config config) {
        for (int i = 0; i < config.numAllyRobots; i++) {
            virtualBotCmdSubs.add(new FieldPublisher<VirtualBotCmds>("From:VirtualBot", "Cmd " + i,
                    new VirtualBotCmds()));
        }
    }

    @Override
    public boolean test(Config config) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("id: ");
//            int id = scanner.nextInt();
            int id = 3;
            System.out.printf("velX: ");
//            float velX = scanner.nextFloat();
            float velX = 10;
            System.out.print("velY: ");
//            float velY = scanner.nextFloat();
            float velY = 10;
            System.out.print("velAng: ");
//            float velAng = scanner.nextFloat();
            float velAng = 1;

            VirtualBotCmds botCmds = new VirtualBotCmds();
            botCmds.setVelX(velX);
            botCmds.setVelY(velY);
            botCmds.setVelAng(velAng);

            virtualBotCmdSubs.get(id).publish(botCmds);
        }
    }
}
