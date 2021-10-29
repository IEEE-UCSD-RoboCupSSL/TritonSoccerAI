package triton.manualTests.periphMiscTests.periphTests;

import triton.App;
import triton.config.Config;
import triton.config.globalVariblesAndConstants.GvcModuleFreqs;
import triton.manualTests.TritonTestable;
import triton.periphModules.display.Display;
import triton.Util;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DisplayTest implements TritonTestable {
    public boolean test(Config config) {
        Display display = new Display(config);
        ScheduledFuture<?> future = App.threadPool.scheduleAtFixedRate(display,
                0,
                Util.toPeriod(GvcModuleFreqs.DISPLAY_MODULE_FREQ, TimeUnit.NANOSECONDS),
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
