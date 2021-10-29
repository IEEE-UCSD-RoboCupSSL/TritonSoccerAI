package triton.manualTests.periphMiscTests.periphTests;

import triton.App;
import triton.config.Config;
import triton.config.globalVariblesAndConstants.GvcModuleFreqs;
import triton.manualTests.TritonTestable;
import triton.periphModules.gameControl.GameCtrlModule;
import triton.periphModules.gameControl.SSLGameCtrlModule;

import static triton.Util.delay;

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
