package Triton.WorldSim;

import Triton.Detection.DetectionData;
import Triton.Detection.DetectionPublisher;
import Triton.Detection.Team;
import Triton.Command.CommandData;
import Triton.Command.Command;
import Proto.MessagesRobocupSslDetection.SSL_DetectionBall;
import Proto.MessagesRobocupSslDetection.SSL_DetectionRobot;

public class FakeDetectionPublisher extends DetectionPublisher implements Runnable {

    DetectionData detect;
    CommandData command;
    WorldSim world;

    public FakeDetectionPublisher() {
    }

    public void run() {
        while (detect == null || command == null) {
            if (detect == null)
                try {
                    detect = DetectionData.get();
                } catch (NullPointerException e) {
                    // do nothing
                }

            if (command == null)
                try {
                    command = CommandData.get();
                } catch (NullPointerException e) {
                    // do nothing
                }
        }

        while (true) {
            try {
                world = new WorldSim();
                Command.setWorld(world);
                break;
            } catch (Exception e) {
                // 
            }
        }

        while (true) {
            try {
                update();
                detect.publish();
            } catch (Exception e) {
                // Do nothing
            }
        }
    }

    public void update() {
        command.executeAll();
        double delta = System.currentTimeMillis() / 1000.0 - world.getTime();
        world.update(delta);

        detect.updateTime(world.getTime());
        detect.setBallCount(world.getBallsCount());
        if (detect.getBallCount() > 0) {
            SSL_DetectionBall.Builder ballBuilder = SSL_DetectionBall.newBuilder();
            ballBuilder.setX((float) world.getBall().getPos().x);
            ballBuilder.setY((float) world.getBall().getPos().y);

            SSL_DetectionBall detectionBall = ballBuilder.build();
            detect.updateBall(detectionBall, world.getTime());
        }

        for (RobotSim robot : world.getRobots().get(Team.BLUE).values())
            updateRobot(robot);
        for (RobotSim robot : world.getRobots().get(Team.YELLOW).values())
            updateRobot(robot);
    }

    public void updateRobot(RobotSim robot) {
        SSL_DetectionRobot.Builder robotBuilder = SSL_DetectionRobot.newBuilder();
        robotBuilder.setX((float) robot.getPos().x);
        robotBuilder.setY((float) robot.getPos().y);
        robotBuilder.setOrientation((float) robot.getOrient());

        SSL_DetectionRobot detectionRobot = robotBuilder.build();
        detect.updateRobot(robot.getTeam(), robot.getID(), detectionRobot, world.getTime());
    }
}