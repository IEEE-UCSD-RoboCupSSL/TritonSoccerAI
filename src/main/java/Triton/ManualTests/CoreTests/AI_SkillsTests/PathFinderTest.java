package Triton.ManualTests.CoreTests.AI_SkillsTests;

import Triton.App;
import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.TritonTestable;
import Triton.PeriphModules.Display.Display;
import Triton.Util;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PathFinderTest implements TritonTestable {

    RobotList<Ally> fielders;
    Ally keeper;
    Ball ball;

    public PathFinderTest(RobotList<Ally> fielders, Ally keeper, Ball ball) {
        this.ball = ball;
        this.fielders = fielders;
        this.keeper = keeper;
    }

    public boolean test(Config config) {
        Display display = this.fielders.get(0).displayPathFinder();
        //this.keeper.curveTo(new Vec2D(0, 0), 0);
        //Display display = this.keeper.displayPathFinder();

        ScheduledFuture<?> displayFuture = App.threadPool.scheduleAtFixedRate(display, 0,
                Util.toPeriod(GvcModuleFreqs.DISPLAY_MODULE_FREQ, TimeUnit.NANOSECONDS),
                TimeUnit.NANOSECONDS);

        /*App.threadPool.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        if (display instanceof JPSPathfinderDisplay) {
                            JPSPathfinderDisplay display1 = (JPSPathfinderDisplay) display;
                            System.out.println(display1.getPath());
                        }
                    }
                }, 0,
                Util.toPeriod(GvcModuleFreqs.DISPLAY_MODULE_FREQ, TimeUnit.NANOSECONDS),
                TimeUnit.NANOSECONDS);*/
        return true;
    }
}
