package triton.manualTests.coreTests.aiSkillsTests;

import triton.config.Config;
import triton.coreModules.ai.skills.ShootGoal;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.RobotList;
import triton.manualTests.coreTests.robotSkillsTests.RobotSkillsTest;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.ArrayList;
import java.util.Scanner;

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
