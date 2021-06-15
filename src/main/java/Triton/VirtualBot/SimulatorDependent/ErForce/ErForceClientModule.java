package Triton.VirtualBot.SimulatorDependent.ErForce;


import Triton.Config.Config;
import Triton.VirtualBot.SimClientModule;
import Triton.VirtualBot.VirtualBotCmds;

import java.util.ArrayList;

public class ErForceClientModule extends SimClientModule {

    public ErForceClientModule(Config config) {
        super(config);
    }

    @Override
    protected void sendCommandsToSim() {
        for (int i = 0; i < config.numAllyRobots; i++) {
            VirtualBotCmds cmd = virtualBotCmdSubs.get(i).getMsg();
        }

//        byte[] bytes;
//        bytes = packet.toByteArray();
//        send(bytes);
    }
}
