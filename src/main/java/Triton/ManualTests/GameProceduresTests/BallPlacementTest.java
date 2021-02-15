package Triton.ManualTests.GameProceduresTests;

import Triton.CoreModules.AI.Estimators.GapFinder;
import Triton.CoreModules.AI.GameProcedures.BallPlacement;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.PeriphModules.Display.Display;
import Triton.PeriphModules.Display.PaintOption;

import java.util.ArrayList;

import static Triton.PeriphModules.Display.PaintOption.*;

public class BallPlacementTest {
    Ally fielder;
    Ball ball;

    public BallPlacementTest(Ally fielder, Ball ball) {
        this.fielder = fielder;
        this.ball = ball;
    }

    public boolean test() {
        while (!BallPlacement.placeBall(fielder, ball, new Vec2D(0, 0))) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        fielder.moveAt(new Vec2D(0, -10));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        fielder.stop();
        return true;
    }
}
