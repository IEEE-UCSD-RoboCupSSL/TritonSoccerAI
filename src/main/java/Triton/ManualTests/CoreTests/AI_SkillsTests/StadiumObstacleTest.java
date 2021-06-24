package Triton.ManualTests.CoreTests.AI_SkillsTests;

import Triton.App;
import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.CoreModules.AI.PathFinder.JumpPointSearch.JPSPathFinder;
import Triton.ManualTests.TritonTestable;
import Triton.PeriphModules.Display.StadiumObstacleDisplay;
import Triton.Util;

import java.util.concurrent.TimeUnit;

import static Triton.Config.GlobalVariblesAndConstants.GvcGeometry.FIELD_WIDTH;
import static Triton.Config.GlobalVariblesAndConstants.GvcGeometry.FIELD_LENGTH;

public class StadiumObstacleTest implements TritonTestable {

    public boolean test(Config config) {

        JPSPathFinder JPS = new JPSPathFinder(FIELD_WIDTH, FIELD_LENGTH, config, false);
        StadiumObstacleDisplay display = new StadiumObstacleDisplay(JPS, config);

        App.threadPool.scheduleAtFixedRate(display, 0,
                Util.toPeriod(GvcModuleFreqs.DISPLAY_MODULE_FREQ, TimeUnit.NANOSECONDS),
                TimeUnit.NANOSECONDS);

        return true;
    }

}
