package Triton.VirtualBot.SimulatorDependent.GrSim_OldProto;
import Triton.Config.Config;
import Triton.VirtualBot.SimClientModule;

import static Triton.Util.delay;

public class GrSimClientModule implements SimClientModule {


    private boolean isFirstRun = true;

    public GrSimClientModule(Config config) {

    }

    private void setup() {

    }

    @Override
    public void run() {
        if (isFirstRun) {
            setup();
            isFirstRun = false;
        }

        delay(1000);
        // ...
    }
}
