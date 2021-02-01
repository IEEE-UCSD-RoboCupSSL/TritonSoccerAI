package Triton.ManualTests.AISkillsTests;

import Triton.CoreModules.AI.AI_Skills.SwarmMoves;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.RobotSkillsTests.RobotSkillsTest;
import Triton.Misc.Coordinates.Vec2D;

import java.util.ArrayList;
import java.util.Scanner;

public class GroupToTest extends RobotSkillsTest {
    Scanner scanner;
    RobotList<Ally> allies;

    public GroupToTest(Scanner scanner, RobotList<Ally> allies) {
        this.scanner = scanner;
        this.allies = (RobotList<Ally>) allies.clone();
    }

    @Override
    public boolean test() {
        try {
            ArrayList<Vec2D> posList = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                double xMin = -2500;
                double xMax = 2500;
                double yMin = -4500;
                double yMax = 4500;

                double x =  (int) ((Math.random() * (xMax - xMin)) + xMin);
                double y =  (int) ((Math.random() * (yMax - yMin)) + yMin);
                posList.add(new Vec2D(x, y));
            }

            while (!SwarmMoves.groupTo(allies, posList));

            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }
}
