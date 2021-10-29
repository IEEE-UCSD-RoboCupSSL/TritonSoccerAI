package triton.manualTests.periphMiscTests.periphTests;

import triton.App;
import triton.config.Config;
import triton.config.globalVariblesAndConstants.GvcModuleFreqs;
import triton.manualTests.TritonTestable;
import triton.periphModules.gameControl.GameCtrlModule;
import triton.periphModules.gameControl.PySocketGameCtrlModule;
import triton.Util;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static triton.coreModules.robot.Team.BLUE;

public class PySocketGameCtrlModuleTest implements TritonTestable {
    public boolean test(Config config) {
        int port = (config.myTeam == BLUE) ? 6543 : 6544;
        GameCtrlModule gameCtrlModule = new PySocketGameCtrlModule(port);
        ScheduledFuture<?> future = App.threadPool.scheduleAtFixedRate(gameCtrlModule,
                0,
                Util.toPeriod(GvcModuleFreqs.GAME_CTRL_MODULE_FREQ, TimeUnit.NANOSECONDS),
                TimeUnit.NANOSECONDS);

        long time = System.currentTimeMillis();
        while (true) {
            System.out.println((System.currentTimeMillis() - time) + ": " + gameCtrlModule.getGameState());
        }
    }
}
