package Triton.ManualTests.RobotSkillsTests.AsyncSkillsTests;

import Triton.App;
import Triton.Config.Config;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.ProceduralSkills.Dependency.ProceduralTask;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.TritonTestable;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.ModulePubSubSystem.FieldPubSubPair;

import java.util.Scanner;
import java.util.concurrent.Future;

public class SimpleProceduralSkillDemo implements TritonTestable {

    private final RobotList<Ally> fielders;
    private Ball ball;


    public SimpleProceduralSkillDemo(RobotList<Ally> fielders, Ball ball) {
        this.fielders = fielders;
        this.ball = ball;
    }


    public boolean test(Config config) {
        Scanner scanner = new Scanner(System.in);
        ExampleProceduralTaskA taskA = new ExampleProceduralTaskA(); // example side thread job
        ExampleProceduralTaskB taskB = new ExampleProceduralTaskB(ball); // another example side thread job

        /* use a separate thread to monitor keyboard input cmd */
        FieldPubSubPair<String> cmdPubSub = new FieldPubSubPair<>("From:SimpleProceduralSkillDemo", "Cmd", "");
        Future<Boolean> cmdFuture = App.threadPool.submit(() -> {
            while(true) {
                System.out.println(">>> (For TaskA Only) Enter q to quit, +/- to (in/de)crease spin speed, " +
                        "c to observe independent procedure got interrupted hence canceled in the middle of execution");
                String cmd = scanner.nextLine();
                cmdPubSub.pub.publish(cmd);
                if(cmd.equals("q")) break;
            }
        }, true);


        Ally botA = fielders.get(0);
        Ally botB = fielders.get(1);

        double baseSpd = 0;


        boolean quit = false;
        while(!quit) {

            /* example main thread job */

            if(!botA.isProcedureCompleted()) {
                botA.executeProceduralTask(taskA);
            } else {
                if(botA.isProcedureCancelled()) {
                    System.out.println("TaskA Interrupted and Canceled! :(");
                } else {
                   System.out.println("Procedural TaskA Completed! :)");
                }
                botA.resetProceduralTask();
            }


            if(!botB.isProcedureCompleted()) {
                botB.executeProceduralTask(taskB);
            } else {
                if(botB.isProcedureCancelled()) {
                    System.out.println("TaskB Interrupted and Canceled! :(");
                } else {
                    System.out.println("Procedural TaskB Completed! :)");
                }
                botB.resetProceduralTask();
            }


            for(Ally bot : fielders) {
                if(bot != botA && bot != botB) { // rest of bots
                    bot.spinAt(15);
                    bot.moveAt(new Vec2D(0,0));
                }
            }


            /* stdin cmd parsing */
            switch (cmdPubSub.sub.getMsg()) {
                case "q" -> {
                    quit = true;
                    botA.cancelProceduralTask();
                    botB.cancelProceduralTask();
                    cmdPubSub.pub.publish("");
                }
                case "+" -> {
                    taskA.setSpinSpeed(++baseSpd);
                    System.out.println("CurrentSpinSpeed: " + baseSpd);
                    cmdPubSub.pub.publish("");
                }
                case "-" -> {
                    taskA.setSpinSpeed(--baseSpd);
                    System.out.println("CurrentSpinSpeed: " + baseSpd);
                    cmdPubSub.pub.publish("");
                }
                case "c" -> {
                    botA.cancelProceduralTask();
                    botA.stop();
                    botB.cancelProceduralTask();
                    botB.stop();
                    cmdPubSub.pub.publish("");
                }
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


        fielders.stopAll();


        return true;
    }



    private static class ExampleProceduralTaskA extends ProceduralTask {

        private final FieldPubSubPair<Double> spinSpeedPubSub;

        private final double length = 1200;
        private final Vec2D posA = new Vec2D(length, 0);
        private final Vec2D posB = new Vec2D(0, length);
        private final Vec2D posC = new Vec2D(-length, 0);


        public ExampleProceduralTaskA() {
            spinSpeedPubSub = new FieldPubSubPair<>("From:SimpleProceduralSkillDemo", "Speed", 0.00);
        }

        public void setSpinSpeed(double spd) {
            spinSpeedPubSub.pub.publish(spd);
        }

        private double getSpinSpeed() {
            return spinSpeedPubSub.sub.getMsg();
        }


        @Override
        public Boolean call() throws Exception {

            if(!gotoPosX(posA)) return false;
            if(!gotoPosX(posB)) return false;
            if(!gotoPosX(posC)) return false;

            return true;
        }

        private boolean gotoPosX(Vec2D posX) {
            while(!thisRobot.isPosArrived(posX)) {
                if(isCancelled()) return false;

                thisRobot.moveTo(posX);
                thisRobot.spinAt(getSpinSpeed());

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class ExampleProceduralTaskB extends ProceduralTask {
        Ball ball;
        public ExampleProceduralTaskB(Ball ball) {
            this.ball = ball;
        }

        @Override
        public Boolean call() throws Exception {

            while(!thisRobot.isHoldingBall()) {
                thisRobot.getBall(ball);
                if(isCancelled()) return false;
                try { Thread.sleep(1); } catch (InterruptedException e) { return false; }
            }

            thisRobot.stop();
            Thread.sleep(50);

            Vec2D toCenter = new Vec2D(0, 0).sub(ball.getPos());
            while(!thisRobot.isDirAimed(toCenter.toPlayerAngle())) {
                thisRobot.rotateTo(toCenter.toPlayerAngle());
                if(isCancelled()) return false;
                try { Thread.sleep(1); } catch (InterruptedException e) { return false; }
            }

            thisRobot.stop();
            thisRobot.kick(new Vec2D(2.5, 2.5));
            Thread.sleep(50);
            thisRobot.stop();

            return true;
        }

    }


}
