package Triton.ManualTests.PeriphTests;

import Triton.App;
import Triton.Config.ModuleFreqConfig;
import Triton.PeriphModules.Display.Display;
import Triton.Util;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DisplayTest {
    public boolean test() {
        Display display = new Display();
        ScheduledFuture<?> future = App.threadPool.scheduleAtFixedRate(display,
                0,
                Util.toPeriod(ModuleFreqConfig.DISPLAY_MODULE_FREQ, TimeUnit.NANOSECONDS),
                TimeUnit.NANOSECONDS);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        future.cancel(false);
        return true;
    }
}
