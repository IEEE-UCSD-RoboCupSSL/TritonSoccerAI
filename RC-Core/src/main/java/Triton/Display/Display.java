package Triton.Display;

import Triton.Detection.*;
import Triton.Geometry.*;
import Triton.Shape.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.util.*;
import java.util.Timer;

import javax.swing.*;
import Proto.MessagesRobocupSslGeometry.SSL_FieldCicularArc;

public class Display extends JPanel {
    private static final double SCALE = 1.0 / 10.0;
    private static final double ROBOT_RADIUS = 90;
    private static final int ROBOT_RADIUS_PIXELS = (int) (ROBOT_RADIUS * SCALE);
    private static final double BALL_RADIUS = 45;
    private static final int BALL_RADIUS_PIXELS = (int) (BALL_RADIUS * SCALE);
    private static final int ROBOT_OUTLINE_THICKNESS = 2;
    private static final int BALL_OUTLINE_THICKNESS = 1;

    private static final int TARGET_FPS = 120;
    private static final long UPDATE_DELAY = 1000 / TARGET_FPS; // ms

    private static int windowWidth;
    private static int windowHeight;
    private static Field field;

    private JFrame frame;

    private BufferedImage yellowRobotImg;
    private BufferedImage blueRobotImg;
    private BufferedImage ballImg;
    private long lastPaint;

    private class RepaintTask extends TimerTask {
        private Display display;

        public RepaintTask(Display display) {
            this.display = display;
        }

        @Override
        public void run() {
            display.paintImmediately(0, 0, display.windowWidth, display.windowHeight);
        }
    }

    public Display() {
        super();
        frame = new JFrame("Display");
        frame.add(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.decode("#234823"));
        loadImages();
        start();
    }

    public void start() {
        while (true) {
            try {
                field = GeometryData.get().getField();
                if (field.fieldLength == 0 || field.fieldWidth == 0 || field.goalDepth == 0)
                    continue;
                windowWidth = (int) ((field.fieldLength + field.goalDepth * 2.0) * SCALE);
                windowHeight = (int) (field.fieldWidth * SCALE);
                break;
            } catch (Exception e) {
                // Geometry not ready, do nothing
            }
        }

        frame.setSize(windowWidth, windowHeight);
        frame.setVisible(true);

        Timer repaintTimer = new Timer();
        repaintTimer.scheduleAtFixedRate(new RepaintTask(this), 0, UPDATE_DELAY);
    }

    private void loadImages() {
        yellowRobotImg = new BufferedImage((ROBOT_RADIUS_PIXELS + ROBOT_OUTLINE_THICKNESS) * 2,
                (ROBOT_RADIUS_PIXELS + ROBOT_OUTLINE_THICKNESS) * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D yellowRobotGraphics = (Graphics2D) yellowRobotImg.getGraphics();
        yellowRobotGraphics.setColor(Color.RED);
        yellowRobotGraphics.fillOval(0, 0, ROBOT_RADIUS_PIXELS * 2, ROBOT_RADIUS_PIXELS * 2);
        yellowRobotGraphics.setColor(Color.YELLOW);
        yellowRobotGraphics.fillOval(ROBOT_RADIUS_PIXELS / 2, 0, ROBOT_RADIUS_PIXELS, ROBOT_RADIUS_PIXELS);
        yellowRobotGraphics.setColor(Color.WHITE);
        yellowRobotGraphics.setStroke(new BasicStroke(ROBOT_OUTLINE_THICKNESS));
        yellowRobotGraphics.drawOval(0, 0, ROBOT_RADIUS_PIXELS * 2, ROBOT_RADIUS_PIXELS * 2);

        blueRobotImg = new BufferedImage((ROBOT_RADIUS_PIXELS + ROBOT_OUTLINE_THICKNESS) * 2,
                (ROBOT_RADIUS_PIXELS + ROBOT_OUTLINE_THICKNESS) * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D blueRobotGraphics = (Graphics2D) blueRobotImg.getGraphics();
        blueRobotGraphics.setColor(Color.BLUE);
        blueRobotGraphics.fillOval(0, 0, ROBOT_RADIUS_PIXELS * 2, ROBOT_RADIUS_PIXELS * 2);
        blueRobotGraphics.setColor(Color.CYAN);
        blueRobotGraphics.fillOval(ROBOT_RADIUS_PIXELS / 2, 0, ROBOT_RADIUS_PIXELS, ROBOT_RADIUS_PIXELS);
        blueRobotGraphics.setColor(Color.WHITE);
        blueRobotGraphics.setStroke(new BasicStroke(ROBOT_OUTLINE_THICKNESS));
        blueRobotGraphics.drawOval(0, 0, ROBOT_RADIUS_PIXELS * 2, ROBOT_RADIUS_PIXELS * 2);

        ballImg = new BufferedImage((BALL_RADIUS_PIXELS + BALL_OUTLINE_THICKNESS) * 2,
                (BALL_RADIUS_PIXELS + BALL_OUTLINE_THICKNESS) * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ballGraphics = (Graphics2D) ballImg.getGraphics();
        ballGraphics.setColor(Color.MAGENTA);
        ballGraphics.fillOval(0, 0, BALL_RADIUS_PIXELS * 2, BALL_RADIUS_PIXELS * 2);
        ballGraphics.setColor(Color.WHITE);
        ballGraphics.setStroke(new BasicStroke(BALL_OUTLINE_THICKNESS));
        ballGraphics.drawOval(0, 0, BALL_RADIUS_PIXELS * 2, BALL_RADIUS_PIXELS * 2);
    }

    public int[] convert(Vec2D v) {
        int[] res = { (int) (v.x * SCALE + windowWidth / 2), (int) (-v.y * SCALE + windowHeight / 2) };
        return res;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        field.lineSegments.forEach((name, line) -> {
            int[] p1 = convert(line.p1);
            int[] p2 = convert(line.p2);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(p1[0], p1[1], p2[0], p2[1]);
        });

        for (SSL_FieldCicularArc arc : field.arcList) {
            int[] center = convert(new Vec2D(arc.getCenter().getX(), arc.getCenter().getY()));
            int radius = (int) (arc.getRadius() * SCALE);

            g2d.drawArc(center[0] - radius, center[1] - radius, radius * 2, radius * 2,
                    (int) Math.toDegrees(arc.getA1()), (int) Math.toDegrees(arc.getA2()));
        }

        for (int i = 0; i < 6; i++) {
            int[] pos = convert(DetectionData.get().getRobotPos(Team.YELLOW, i));
            // double orient = DetectionData.get().getRobotOrient(Team.YELLOW, i);
            // g2d.rotate(orient);
            g2d.drawImage(yellowRobotImg, pos[0] - yellowRobotImg.getWidth() / 2,
                    pos[1] - yellowRobotImg.getHeight() / 2, this);
            // g2d.rotate(-orient);
            g2d.setColor(Color.WHITE);
            g2d.drawString(Integer.toString(i), pos[0], pos[1] - ROBOT_RADIUS_PIXELS * 2);
        }

        for (int i = 0; i < 6; i++) {
            int[] pos = convert(DetectionData.get().getRobotPos(Team.BLUE, i));
            // double orient = DetectionData.get().getRobotOrient(Team.BLUE, i);
            // g2d.rotate(orient);
            g2d.drawImage(blueRobotImg, pos[0] - blueRobotImg.getWidth() / 2, pos[1] - blueRobotImg.getHeight() / 2,
                    this);
            // g2d.rotate(-orient);
            g2d.setColor(Color.WHITE);
            g2d.drawString(Integer.toString(i), pos[0], pos[1] - ROBOT_RADIUS_PIXELS * 2);
        }

        int[] ballPos = convert(DetectionData.get().getBallPos());
        g2d.drawImage(ballImg, ballPos[0], ballPos[1], this);

        g2d.drawString("LAST UPDATE: " + (System.currentTimeMillis() - lastPaint) + " ms", 50, windowHeight - 70);
        g2d.drawString(String.format("LAST UPDATE: %d ms", System.currentTimeMillis() - lastPaint), 50,
                windowHeight - 70);
        g2d.drawString(String.format("FPS: %.2f", 1000.0 / (System.currentTimeMillis() - lastPaint)), 50,
                windowHeight - 50);
        lastPaint = System.currentTimeMillis();
    }
}