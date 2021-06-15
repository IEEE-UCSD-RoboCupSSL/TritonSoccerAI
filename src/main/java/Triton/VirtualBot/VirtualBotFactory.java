package Triton.VirtualBot;

import Triton.Config.Config;

public class VirtualBotFactory {

    private static VirtualBotList vbots;

    /* should only create them once */
    public static VirtualBotList createVirtualBots(Config config) {
        /* Instantiate & run virtualbot modules to run vbots on a simulator */
        vbots = new VirtualBotList();
        for (int id = 0; id < config.numAllyRobots; id++) {
            VirtualBot vbot = new VirtualBot(config, id);
            vbots.add(vbot);
        }
        return vbots;
    }

    public static void pauseAllVirtualBots() {
        for(VirtualBot vbot : vbots) {
            vbot.pauseBot();
        }
    }

    public static void resumeAllVirtualBots() {
        for(VirtualBot vbot : vbots) {
            vbot.resumeBot();
        }
    }


}
