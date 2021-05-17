package Triton.ManualTests.AI_SkillsTests;

import Triton.CoreModules.AI.AI_Skills.ShootGoal;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.RobotSkillsTests.RobotSkillsTest;
import Triton.Misc.Math.Matrix.Vec2D;

import java.util.ArrayList;
import java.util.Scanner;

public class ShootGoalTest extends RobotSkillsTest {
    Scanner scanner;
    Ally shooter;
    RobotList<Foe> foes;
    Ball ball;

    ShootGoal shootGoal;

    public ShootGoalTest(Scanner scanner, Ally shooter, RobotList<Foe> foes, Ball ball) {
        this.scanner = scanner;
        this.shooter = shooter;
        this.foes = foes;
        this.ball = ball;

        shootGoal = new ShootGoal(shooter, foes, ball);
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
