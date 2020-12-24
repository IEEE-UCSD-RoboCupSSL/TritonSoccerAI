package Triton.Display;

import Triton.Config.DisplayConfig;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImgLoader {
    public static BufferedImage yellowRobot;
    public static BufferedImage blueRobot;
    public static BufferedImage ball;
    public static BufferedImage startPoint;
    public static BufferedImage desPoint;

    public static void loadImages() {
        yellowRobot = new BufferedImage((DisplayConfig.ROBOT_RADIUS_PIXELS + DisplayConfig.ROBOT_OUTLINE_THICKNESS) * 2,
                (DisplayConfig.ROBOT_RADIUS_PIXELS + DisplayConfig.ROBOT_OUTLINE_THICKNESS) * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D yellowRobotGraphics = (Graphics2D) yellowRobot.getGraphics();
        yellowRobotGraphics.setColor(Color.decode("#ff8c00"));
        yellowRobotGraphics.fillOval(0, 0, DisplayConfig.ROBOT_RADIUS_PIXELS * 2, DisplayConfig.ROBOT_RADIUS_PIXELS * 2);
        yellowRobotGraphics.setColor(Color.BLACK);
        yellowRobotGraphics.fillOval(DisplayConfig.ROBOT_RADIUS_PIXELS, DisplayConfig.ROBOT_RADIUS_PIXELS / 2, DisplayConfig.ROBOT_RADIUS_PIXELS,
                DisplayConfig.ROBOT_RADIUS_PIXELS);

        yellowRobotGraphics.setColor(Color.WHITE);
        yellowRobotGraphics.setStroke(new BasicStroke(DisplayConfig.ROBOT_OUTLINE_THICKNESS));
        yellowRobotGraphics.drawOval(0, 0, DisplayConfig.ROBOT_RADIUS_PIXELS * 2, DisplayConfig.ROBOT_RADIUS_PIXELS * 2);
        yellowRobotGraphics.drawOval(DisplayConfig.ROBOT_RADIUS_PIXELS, DisplayConfig.ROBOT_RADIUS_PIXELS / 2, DisplayConfig.ROBOT_RADIUS_PIXELS,
                DisplayConfig.ROBOT_RADIUS_PIXELS);

        blueRobot = new BufferedImage((DisplayConfig.ROBOT_RADIUS_PIXELS + DisplayConfig.ROBOT_OUTLINE_THICKNESS) * 2,
                (DisplayConfig.ROBOT_RADIUS_PIXELS + DisplayConfig.ROBOT_OUTLINE_THICKNESS) * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D blueRobotGraphics = (Graphics2D) blueRobot.getGraphics();
        blueRobotGraphics.setColor(Color.decode("#004FFF"));
        blueRobotGraphics.fillOval(0, 0, DisplayConfig.ROBOT_RADIUS_PIXELS * 2, DisplayConfig.ROBOT_RADIUS_PIXELS * 2);
        blueRobotGraphics.setColor(Color.BLACK);
        blueRobotGraphics.fillOval(DisplayConfig.ROBOT_RADIUS_PIXELS, DisplayConfig.ROBOT_RADIUS_PIXELS / 2, DisplayConfig.ROBOT_RADIUS_PIXELS,
                DisplayConfig.ROBOT_RADIUS_PIXELS);

        blueRobotGraphics.setColor(Color.WHITE);
        blueRobotGraphics.setStroke(new BasicStroke(DisplayConfig.ROBOT_OUTLINE_THICKNESS));
        blueRobotGraphics.drawOval(0, 0, DisplayConfig.ROBOT_RADIUS_PIXELS * 2, DisplayConfig.ROBOT_RADIUS_PIXELS * 2);
        blueRobotGraphics.drawOval(DisplayConfig.ROBOT_RADIUS_PIXELS, DisplayConfig.ROBOT_RADIUS_PIXELS / 2, DisplayConfig.ROBOT_RADIUS_PIXELS,
                DisplayConfig.ROBOT_RADIUS_PIXELS);

        ball = new BufferedImage((DisplayConfig.BALL_RADIUS_PIXELS + DisplayConfig.BALL_OUTLINE_THICKNESS) * 2,
                (DisplayConfig.BALL_RADIUS_PIXELS + DisplayConfig.BALL_OUTLINE_THICKNESS) * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ballGraphics = (Graphics2D) ball.getGraphics();
        ballGraphics.setColor(Color.decode("#FF007F"));
        ballGraphics.fillOval(0, 0, DisplayConfig.BALL_RADIUS_PIXELS * 2, DisplayConfig.BALL_RADIUS_PIXELS * 2);

        ballGraphics.setColor(Color.WHITE);
        ballGraphics.setStroke(new BasicStroke(DisplayConfig.BALL_OUTLINE_THICKNESS));
        ballGraphics.drawOval(0, 0, DisplayConfig.BALL_RADIUS_PIXELS * 2, DisplayConfig.BALL_RADIUS_PIXELS * 2);

        startPoint = new BufferedImage((DisplayConfig.BALL_RADIUS_PIXELS + DisplayConfig.BALL_OUTLINE_THICKNESS) * 2,
                (DisplayConfig.BALL_RADIUS_PIXELS + DisplayConfig.BALL_OUTLINE_THICKNESS) * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D startPointGraphics = (Graphics2D) startPoint.getGraphics();
        startPointGraphics.setColor(Color.GRAY);
        startPointGraphics.fillOval(0, 0, DisplayConfig.BALL_RADIUS_PIXELS * 2, DisplayConfig.BALL_RADIUS_PIXELS * 2);

        startPointGraphics.setColor(Color.WHITE);
        startPointGraphics.setStroke(new BasicStroke(DisplayConfig.BALL_OUTLINE_THICKNESS));
        startPointGraphics.drawOval(0, 0, DisplayConfig.BALL_RADIUS_PIXELS * 2, DisplayConfig.BALL_RADIUS_PIXELS * 2);

        desPoint = new BufferedImage((DisplayConfig.BALL_RADIUS_PIXELS + DisplayConfig.BALL_OUTLINE_THICKNESS) * 2,
                (DisplayConfig.BALL_RADIUS_PIXELS + DisplayConfig.BALL_OUTLINE_THICKNESS) * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D desPointGraphics = (Graphics2D) desPoint.getGraphics();
        desPointGraphics.setColor(Color.RED);
        desPointGraphics.fillOval(0, 0, DisplayConfig.BALL_RADIUS_PIXELS * 2, DisplayConfig.BALL_RADIUS_PIXELS * 2);

        desPointGraphics.setColor(Color.WHITE);
        desPointGraphics.setStroke(new BasicStroke(DisplayConfig.BALL_OUTLINE_THICKNESS));
        desPointGraphics.drawOval(0, 0, DisplayConfig.BALL_RADIUS_PIXELS * 2, DisplayConfig.BALL_RADIUS_PIXELS * 2);
    }
}