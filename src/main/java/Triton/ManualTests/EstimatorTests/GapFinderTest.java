package Triton.ManualTests.EstimatorTests;

import Triton.CoreModules.AI.Estimators.GapFinder;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Subscriber;
import Triton.PeriphModules.Display.Display;
import Triton.PeriphModules.Display.PaintOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import static Triton.PeriphModules.Display.PaintOption.*;

public class GapFinderTest {
    private final Subscriber<HashMap<String, Integer>> fieldSizeSub;
    GapFinder gapFinder;

    public GapFinderTest(RobotList<Ally> fielders, RobotList<Foe> foes, Ball ball) {
        fieldSizeSub = new FieldSubscriber<>("geometry", "fieldSize");

        try {
            fieldSizeSub.subscribe(1000);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        while (gapFinder == null) {
            HashMap<String, Integer> fieldSize = fieldSizeSub.getMsg();

            if (fieldSize == null || fieldSize.get("fieldLength") == 0 || fieldSize.get("fieldWidth") == 0)
                continue;

            double worldSizeX = fieldSize.get("fieldWidth");
            double worldSizeY = fieldSize.get("fieldLength");

            gapFinder = new GapFinder(fielders, foes, ball, new Vec2D(100, 100), new Vec2D(-worldSizeX / 2, -worldSizeY / 2));
        }
    }

    public boolean test() {
        Display display = new Display();
        ArrayList<PaintOption> paintOptions = new ArrayList<>();
        paintOptions.add(GEOMETRY);
        paintOptions.add(OBJECTS);
        paintOptions.add(INFO);
        paintOptions.add(PROBABILITY);
        display.setPaintOptions(paintOptions);

        display.setGapFinder(gapFinder);

        return true;
    }
}
