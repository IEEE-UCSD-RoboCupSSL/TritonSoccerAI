package triton.manualTests.virtualBotTests;

import triton.config.Config;
import triton.manualTests.TritonTestable;
import triton.misc.modulePubSubSystem.FieldPublisher;
import triton.misc.modulePubSubSystem.Publisher;
import triton.virtualBot.VirtualBotCmds;
import triton.virtualBot.VirtualBotFactory;

import java.util.ArrayList;
import java.util.Scanner;

public class SimClientModuleTest implements TritonTestable {
    private final ArrayList<Publisher<VirtualBotCmds>> virtualBotCmdPubs = new ArrayList<>();

    @Override
    public boolean test(Config config) {
        for (int i = 0; i < config.numAllyRobots; i++) {
            virtualBotCmdPubs.add(new FieldPublisher<VirtualBotCmds>("From:VirtualBot", "Cmd " + i,
                    new VirtualBotCmds()));
        }


        Scanner scanner = new Scanner(System.in);
        VirtualBotFactory.pauseAllVirtualBots();

        while (true) {
            System.out.print("id: ");
            int id = scanner.nextInt();
            System.out.print("kickX: ");
            float kickX = scanner.nextFloat();
            System.out.print("kickZ: ");
            float kickZ = scanner.nextFloat();
            System.out.printf("velX: ");
            float velX = scanner.nextFloat();
            System.out.print("velY: ");
            float velY = scanner.nextFloat();
            System.out.print("velAng: ");
            float velAng = scanner.nextFloat();
            System.out.println("spinner (true/false): ");
            boolean spinner = scanner.nextBoolean();

            VirtualBotCmds botCmds = new VirtualBotCmds();
            botCmds.setKickX(kickX);
            botCmds.setKickZ(kickZ);
            botCmds.setVelX(velX);
            botCmds.setVelY(velY);
            botCmds.setVelAng(velAng);
            botCmds.setSpinner(spinner);

            virtualBotCmdPubs.get(id).publish(botCmds);
        }
    }
}
