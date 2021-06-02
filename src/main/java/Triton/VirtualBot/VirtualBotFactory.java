package Triton.VirtualBot;

import Triton.Config.Config;

public class VirtualBotFactory {

    public static VirtualBotList createVirtualBots(Config config) {
        /* Instantiate & run virtualbot modules to run vbots on a simulator */
        VirtualBotList vbots = new VirtualBotList();
        for (int id = 0; id < config.numAllyRobots; id++) {
            VirtualBot vbot = new VirtualBot(config, id);
            vbots.add(vbot);
        }
        return vbots;
    }

}
