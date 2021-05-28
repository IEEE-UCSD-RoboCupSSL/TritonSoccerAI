package Triton.ManualTests.PeriphTests;

import Triton.App;
import Triton.Config.Config;
import Triton.Config.OldConfigs.ModuleFreqConfig;
import Triton.ManualTests.TritonTestable;
import Triton.PeriphModules.GameControl.GameCtrlModule;
import Triton.PeriphModules.GameControl.SSLGameCtrlModule;
import Triton.Util;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SSLGameCtrlModuleTest implements TritonTestable {
    public boolean test(Config config) {
        GameCtrlModule gameCtrlModule = new SSLGameCtrlModule();
        ScheduledFuture<?> future = App.threadPool.scheduleAtFixedRate(gameCtrlModule,
                0,
                Util.toPeriod(ModuleFreqConfig.GAME_CTRL_MODULE_FREQ, TimeUnit.NANOSECONDS),
                TimeUnit.NANOSECONDS);

        long time = System.currentTimeMillis();
        while (true) {
            System.out.println((System.currentTimeMillis() - time) + ": " + gameCtrlModule.getGameState());
        }
    }
}
