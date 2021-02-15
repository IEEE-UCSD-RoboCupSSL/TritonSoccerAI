package Triton.ManualTests.AI_SkillsTests;

import Triton.CoreModules.AI.AI_Skills.ShootGoal;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.RobotSkillsTests.RobotSkillsTest;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.Geometry.Line2D;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Subscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import static Triton.Config.ObjectConfig.MY_TEAM;
import static Triton.CoreModules.Robot.Team.BLUE;

public class ShootGoalTest extends RobotSkillsTest {
    Scanner scanner;
    Ally shooter;
    RobotList<Foe> foes;
    Ball ball;

    Subscriber<HashMap<String, Integer>> fieldSizeSub;
    Subscriber<HashMap<String, Line2D>> fieldLineSub;
    ShootGoal shootGoal;

    public ShootGoalTest(Scanner scanner, Ally shooter, RobotList<Foe> foes, Ball ball) {
        this.scanner = scanner;
        this.shooter = shooter;
        this.foes = foes;
        this.ball = ball;

        fieldSizeSub = new FieldSubscriber<>("geometry", "fieldSize");
        fieldLineSub = new FieldSubscriber<>("geometry", "fieldLines");

        try {
            fieldSizeSub.subscribe(1000);
            fieldLineSub.subscribe(1000);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        while (shootGoal == null) {
            HashMap<String, Integer> fieldSize = fieldSizeSub.getMsg();
            HashMap<String, Line2D> fieldLines = fieldLineSub.getMsg();

            if (fieldSize == null || fieldSize.get("fieldLength") == 0 || fieldSize.get("fieldWidth") == 0)
                continue;

            double worldSizeX = fieldSize.get("fieldWidth");
            double worldSizeY = fieldSize.get("fieldLength");

            double goalXLeft;
            double goalXRight;
            double goalY;
            if (MY_TEAM == BLUE) {
                goalXLeft = PerspectiveConverter.audienceToPlayer(fieldLines.get("RightGoalDepthLine").p2).x;
                goalXRight = PerspectiveConverter.audienceToPlayer(fieldLines.get("RightGoalDepthLine").p1).x;
                goalY = worldSizeY / 2;
            } else {
                goalXLeft = PerspectiveConverter.audienceToPlayer(fieldLines.get("LeftGoalDepthLine").p1).x;
                goalXRight = PerspectiveConverter.audienceToPlayer(fieldLines.get("LeftGoalDepthLine").p2).x;
                goalY = -worldSizeY / 2;
            }

            Vec2D goalLeft = new Vec2D(goalXLeft, goalY);
            Vec2D goalRight = new Vec2D(goalXRight, goalY);
            shootGoal = new ShootGoal(shooter, foes, ball, worldSizeX, worldSizeY,
                    goalLeft.add(new Vec2D(200, 0)), goalRight.add(new Vec2D(-200, 0)));
        }
    }

    @Override
    public boolean test() {
        try {
            while (true) {
                if  (!shooter.isHoldingBall()) {
                    shooter.getBall(ball);
                }
                else {
                    Vec2D startPos = shooter.getPos();
                    ArrayList<Vec2D> shootPosAndTarget = shootGoal.findOptimalShootPos(startPos);
                    if (!shooter.isPosArrived(startPos)) {
                        shooter.curveTo(startPos);
                    }
                    shootGoal.shoot(shootPosAndTarget.get(0), shootPosAndTarget.get(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
