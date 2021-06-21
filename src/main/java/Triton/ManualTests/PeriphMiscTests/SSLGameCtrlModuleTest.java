package Triton.ManualTests.PeriphMiscTests;

import Triton.App;
import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.ManualTests.TritonTestable;
import Triton.PeriphModules.GameControl.GameCtrlModule;
import Triton.PeriphModules.GameControl.SSLGameCtrlModule;
import Triton.Util;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static Triton.Util.delay;

public class SSLGameCtrlModuleTest implements TritonTestable {
    public boolean test(Config config) {
        GameCtrlModule gameCtrlModule = new SSLGameCtrlModule(config);
        App.runModule(gameCtrlModule, GvcModuleFreqs.GAME_CTRL_MODULE_FREQ);

        long time = System.currentTimeMillis();
        while (true) {
            delay(10);
            System.out.println((System.currentTimeMillis() - time) + ": " + gameCtrlModule.getGameState().toString());
        }
    }
}
