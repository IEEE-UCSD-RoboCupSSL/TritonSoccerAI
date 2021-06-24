package Triton.ManualTests.CoreTests.AI_SkillsTests;

import Triton.App;
import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.ManualTests.TritonTestable;
import Triton.PeriphModules.Display.BallPlacementDefenseDisplay;
import Triton.PeriphModules.Display.Display;
import Triton.Util;

import java.util.concurrent.TimeUnit;

public class BallPlacementDefenseTest implements TritonTestable {

    public boolean test(Config config) {
        Display display = new BallPlacementDefenseDisplay(config);

        App.threadPool.scheduleAtFixedRate(display, 0,
                Util.toPeriod(GvcModuleFreqs.DISPLAY_MODULE_FREQ, TimeUnit.NANOSECONDS),
                TimeUnit.NANOSECONDS);

        return true;
    }

}
