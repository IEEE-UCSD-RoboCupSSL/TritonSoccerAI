package Triton.ManualTests.CoreTests.AI_SkillsTests;

import Triton.Config.Config;
import Triton.CoreModules.AI.AI_Skills.ShootGoal;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.CoreTests.RobotSkillsTests.RobotSkillsTest;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

import java.util.ArrayList;
import java.util.Scanner;

import static Triton.Config.OldConfigs.ObjectConfig.MAX_KICK_VEL;

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
    public boolean test(Config config) {
        Scanner scanner = new Scanner(System.in);
        try {
            while (true) {
                if  (!shooter.isHoldingBall()) {
                    shooter.getBall(ball);
                }
                else {
                    Vec2D startPos = shooter.getPos();
                    while (shooter.isHoldingBall()) {
                        ArrayList<Vec2D> shootPosAndTarget = shootGoal.findOptimalShootPos(startPos);
                        if (shootPosAndTarget.get(0) != null && shootPosAndTarget.get(1) != null)
                            shootGoal.shoot(shootPosAndTarget.get(0), shootPosAndTarget.get(1));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
