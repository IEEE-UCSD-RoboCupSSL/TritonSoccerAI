package Triton.ManualTests.AI_SkillsTests;

import Triton.CoreModules.AI.AI_Skills.Swarm;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.RobotSkillsTests.RobotSkillsTest;
import Triton.Misc.Math.Matrix.Vec2D;

import java.util.ArrayList;
import java.util.Scanner;

public class GroupToTest extends RobotSkillsTest {
    Scanner scanner;
    RobotList<Ally> allies;
    Ball ball;

    public GroupToTest(Scanner scanner, RobotList<Ally> allies, Ball ball) {
        this.scanner = scanner;
        //noinspection unchecked
        this.allies = (RobotList<Ally>) allies.clone();
        this.ball = ball;
    }

    @Override
    public boolean test() {
        try {

            Swarm swarm = new Swarm(allies);

            /* To-do: fix sprintTo */
            ArrayList<Double> dirList = new ArrayList<>();
            for(int i = 0; i < 5; i++) dirList.add(0.0);


            System.out.println("Test center prioritized");
            ArrayList<Vec2D> posList = new ArrayList<>();
            posList.add(new Vec2D(2000, 300));
            posList.add(new Vec2D(1000, 0));
            posList.add(new Vec2D(-2000, -500));
            posList.add(new Vec2D(0, -1000));
            posList.add(new Vec2D(-1000, 1000));
            while (!swarm.groupTo(posList, dirList)) {
                Thread.sleep(1);
            }

            Thread.sleep(1000);


            System.out.println("Test Front prioritized");
            posList = new ArrayList<>();
            posList.add(new Vec2D(0, 4000));
            posList.add(new Vec2D(0, 3000));
            posList.add(new Vec2D(2000, 4000));
            posList.add(new Vec2D(0, 1000));
            posList.add(new Vec2D(-2000, 4000));
            while (!swarm.groupTo(posList, dirList, new Vec2D(0, 5000))) {
                Thread.sleep(1);
            }

            Thread.sleep(1000);


            System.out.println("Test BallPosition prioritized");
            while(ball.getPos().y < 4500) {
                do {
                    Thread.sleep(1);
                    posList = new ArrayList<>();
                    posList.add(ball.getPos().add(new Vec2D(0, -300)));
                    posList.add(ball.getPos().add(new Vec2D(0, -600)));
                    posList.add(ball.getPos().add(new Vec2D(0, -900)));
                    posList.add(ball.getPos().add(new Vec2D(501, -1200)));
                    posList.add(ball.getPos().add(new Vec2D(-500, -1200)));
                }while (!swarm.groupTo(posList, dirList, ball.getPos()));
            }



        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }



}
