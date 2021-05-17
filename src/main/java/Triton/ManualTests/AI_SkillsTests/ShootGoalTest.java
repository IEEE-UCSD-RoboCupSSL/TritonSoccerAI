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

import static Triton.Config.GeometryConfig.GOAL_LEFT;
import static Triton.Config.ObjectConfig.MY_TEAM;
import static Triton.CoreModules.Robot.Team.BLUE;

public class ShootGoalTest extends RobotSkillsTest {
    Ally shooter;
    RobotList<Foe> foes;
    Ball ball;

    ShootGoal shootGoal;

    public ShootGoalTest(Ally shooter, RobotList<Foe> foes, Ball ball) {
        this.shooter = shooter;
        this.foes = foes;
        this.ball = ball;

        shootGoal = new ShootGoal(shooter, foes, ball);
    }

    @Override
    public boolean test() {
        Scanner scanner = new Scanner(System.in);
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
