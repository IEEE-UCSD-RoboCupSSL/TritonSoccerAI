package Triton.ManualTests.PeriphTests;

import Triton.App;
import Triton.Config.ModuleFreqConfig;
import Triton.PeriphModules.GameControl.GameCtrlModule;
import Triton.PeriphModules.GameControl.PySocketGameCtrlModule;
import Triton.PeriphModules.GameControl.SSLGameCtrlModule;
import Triton.PeriphModules.GameControl.StdinGameCtrlModule;
import Triton.Util;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static Triton.Config.ObjectConfig.MY_TEAM;
import static Triton.CoreModules.Robot.Team.BLUE;

public class PySocketGameCtrlModuleTest {
    public boolean test() {
        int port = (MY_TEAM == BLUE) ? 6543 : 6544;
        GameCtrlModule gameCtrlModule = new PySocketGameCtrlModule(port);
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
