package Triton.ManualTests.VirtualBotTests;

import Triton.Config.Config;
import Triton.ManualTests.TritonTestable;
import Triton.SoccerObjects;

public class VirtualBotModuleTest implements TritonTestable {
    private final SoccerObjects soccerObjects;
    // private FieldSubscriber<String> debugStrSub;

    public VirtualBotModuleTest(SoccerObjects soccerObjects) {
        this.soccerObjects = soccerObjects;
    }

    @Override
    public boolean test(Config config) {
        return false;
    }
}
