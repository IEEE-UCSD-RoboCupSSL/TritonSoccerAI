package Triton.PeriphModules.Display;

import Triton.Config.GlobalVariblesAndConstants.GvcDisplay;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Creates images to be displayed
 */
public class ImgLoader {
    public static BufferedImage yellowRobot;
    public static BufferedImage blueRobot;
    public static BufferedImage ball;
    public static BufferedImage startPoint;
    public static BufferedImage desPoint;

    /**
     * Generates various images to be displayed
     */
    public static void generateImages() {
        yellowRobot = new BufferedImage((GvcDisplay.ROBOT_RADIUS_PIXELS + GvcDisplay.ROBOT_OUTLINE_THICKNESS) * 2,
                (GvcDisplay.ROBOT_RADIUS_PIXELS + GvcDisplay.ROBOT_OUTLINE_THICKNESS) * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D yellowRobotGraphics = (Graphics2D) yellowRobot.getGraphics();
        yellowRobotGraphics.setColor(Color.decode("#ff8c00"));
        yellowRobotGraphics.fillOval(0, 0, GvcDisplay.ROBOT_RADIUS_PIXELS * 2, GvcDisplay.ROBOT_RADIUS_PIXELS * 2);
        yellowRobotGraphics.setColor(Color.BLACK);
        yellowRobotGraphics.fillOval(GvcDisplay.ROBOT_RADIUS_PIXELS, GvcDisplay.ROBOT_RADIUS_PIXELS / 2, GvcDisplay.ROBOT_RADIUS_PIXELS,
                GvcDisplay.ROBOT_RADIUS_PIXELS);

        yellowRobotGraphics.setColor(Color.WHITE);
        yellowRobotGraphics.setStroke(new BasicStroke(GvcDisplay.ROBOT_OUTLINE_THICKNESS));
        yellowRobotGraphics.drawOval(0, 0, GvcDisplay.ROBOT_RADIUS_PIXELS * 2, GvcDisplay.ROBOT_RADIUS_PIXELS * 2);
        yellowRobotGraphics.drawOval(GvcDisplay.ROBOT_RADIUS_PIXELS, GvcDisplay.ROBOT_RADIUS_PIXELS / 2, GvcDisplay.ROBOT_RADIUS_PIXELS,
                GvcDisplay.ROBOT_RADIUS_PIXELS);

        blueRobot = new BufferedImage((GvcDisplay.ROBOT_RADIUS_PIXELS + GvcDisplay.ROBOT_OUTLINE_THICKNESS) * 2,
                (GvcDisplay.ROBOT_RADIUS_PIXELS + GvcDisplay.ROBOT_OUTLINE_THICKNESS) * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D blueRobotGraphics = (Graphics2D) blueRobot.getGraphics();
        blueRobotGraphics.setColor(Color.decode("#004FFF"));
        blueRobotGraphics.fillOval(0, 0, GvcDisplay.ROBOT_RADIUS_PIXELS * 2, GvcDisplay.ROBOT_RADIUS_PIXELS * 2);
        blueRobotGraphics.setColor(Color.BLACK);
        blueRobotGraphics.fillOval(GvcDisplay.ROBOT_RADIUS_PIXELS, GvcDisplay.ROBOT_RADIUS_PIXELS / 2, GvcDisplay.ROBOT_RADIUS_PIXELS,
                GvcDisplay.ROBOT_RADIUS_PIXELS);

        blueRobotGraphics.setColor(Color.WHITE);
        blueRobotGraphics.setStroke(new BasicStroke(GvcDisplay.ROBOT_OUTLINE_THICKNESS));
        blueRobotGraphics.drawOval(0, 0, GvcDisplay.ROBOT_RADIUS_PIXELS * 2, GvcDisplay.ROBOT_RADIUS_PIXELS * 2);
        blueRobotGraphics.drawOval(GvcDisplay.ROBOT_RADIUS_PIXELS, GvcDisplay.ROBOT_RADIUS_PIXELS / 2, GvcDisplay.ROBOT_RADIUS_PIXELS,
                GvcDisplay.ROBOT_RADIUS_PIXELS);

        ball = new BufferedImage((GvcDisplay.BALL_RADIUS_PIXELS + GvcDisplay.BALL_OUTLINE_THICKNESS) * 2,
                (GvcDisplay.BALL_RADIUS_PIXELS + GvcDisplay.BALL_OUTLINE_THICKNESS) * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ballGraphics = (Graphics2D) ball.getGraphics();
        ballGraphics.setColor(Color.decode("#FF007F"));
        ballGraphics.fillOval(0, 0, GvcDisplay.BALL_RADIUS_PIXELS * 2, GvcDisplay.BALL_RADIUS_PIXELS * 2);

        ballGraphics.setColor(Color.WHITE);
        ballGraphics.setStroke(new BasicStroke(GvcDisplay.BALL_OUTLINE_THICKNESS));
        ballGraphics.drawOval(0, 0, GvcDisplay.BALL_RADIUS_PIXELS * 2, GvcDisplay.BALL_RADIUS_PIXELS * 2);

        startPoint = new BufferedImage((GvcDisplay.BALL_RADIUS_PIXELS + GvcDisplay.BALL_OUTLINE_THICKNESS) * 2,
                (GvcDisplay.BALL_RADIUS_PIXELS + GvcDisplay.BALL_OUTLINE_THICKNESS) * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D startPointGraphics = (Graphics2D) startPoint.getGraphics();
        startPointGraphics.setColor(Color.GRAY);
        startPointGraphics.fillOval(0, 0, GvcDisplay.BALL_RADIUS_PIXELS * 2, GvcDisplay.BALL_RADIUS_PIXELS * 2);

        startPointGraphics.setColor(Color.WHITE);
        startPointGraphics.setStroke(new BasicStroke(GvcDisplay.BALL_OUTLINE_THICKNESS));
        startPointGraphics.drawOval(0, 0, GvcDisplay.BALL_RADIUS_PIXELS * 2, GvcDisplay.BALL_RADIUS_PIXELS * 2);

        desPoint = new BufferedImage((GvcDisplay.BALL_RADIUS_PIXELS + GvcDisplay.BALL_OUTLINE_THICKNESS) * 2,
                (GvcDisplay.BALL_RADIUS_PIXELS + GvcDisplay.BALL_OUTLINE_THICKNESS) * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D desPointGraphics = (Graphics2D) desPoint.getGraphics();
        desPointGraphics.setColor(Color.RED);
        desPointGraphics.fillOval(0, 0, GvcDisplay.BALL_RADIUS_PIXELS * 2, GvcDisplay.BALL_RADIUS_PIXELS * 2);

        desPointGraphics.setColor(Color.WHITE);
        desPointGraphics.setStroke(new BasicStroke(GvcDisplay.BALL_OUTLINE_THICKNESS));
        desPointGraphics.drawOval(0, 0, GvcDisplay.BALL_RADIUS_PIXELS * 2, GvcDisplay.BALL_RADIUS_PIXELS * 2);
    }
}