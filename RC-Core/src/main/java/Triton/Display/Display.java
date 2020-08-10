package Triton.Display;

import Triton.Detection.*;
import Triton.Geometry.*;
import Triton.Shape.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import javax.swing.*;
import Proto.MessagesRobocupSslGeometry.SSL_FieldCicularArc;

public class Display extends JPanel implements Runnable {
    private static final double SCALE = 1.0 / 10.0;
    private static final double ROBOT_RADIUS = 90;
    private static final int ROBOT_RADIUS_PIXELS = (int) (ROBOT_RADIUS * SCALE);
    private static final double BALL_RADIUS = 45;
    private static final int BALL_RADIUS_PIXELS = (int) (BALL_RADIUS * SCALE);
    private static final int TARGET_FPS = 15;
    private static final long UPDATE_DELAY = 1000 / TARGET_FPS; // ms

    private static int windowWidth;
    private static int windowHeight;
    private static Field field;
    
    BufferedImage yellowRobotImg;
    BufferedImage blueRobotImg;
    BufferedImage ballImg;

    JFrame frame;

    public Display() {
        super();
        frame = new JFrame("Display");
        frame.add(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.decode("#234823"));
        loadImages();
    }

    public void run() {
        while (true) {
            try {
                field = GeometryData.get().getField();
                windowWidth = (int) ((field.fieldLength + field.goalDepth * 2.0) * SCALE);
                windowHeight = (int) (field.fieldWidth * SCALE);
                if (windowWidth == 0 || windowHeight == 0)
                    continue;
                break;
            } catch (Exception e) {
                // Geometry not ready, do nothing
            }
        }

        frame.setSize(windowWidth, windowHeight);
        frame.setVisible(true);

        while (true) {
            repaint();
        }
    }

    private void loadImages() {
        yellowRobotImg = new BufferedImage(ROBOT_RADIUS_PIXELS * 2, ROBOT_RADIUS_PIXELS * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics yellowRobotGraphics = yellowRobotImg.getGraphics();
        yellowRobotGraphics.setColor(Color.YELLOW);
        yellowRobotGraphics.fillOval(0, 0, ROBOT_RADIUS_PIXELS * 2, ROBOT_RADIUS_PIXELS * 2);
        yellowRobotGraphics.setColor(Color.RED);
        yellowRobotGraphics.fillOval(ROBOT_RADIUS_PIXELS / 2, 0, ROBOT_RADIUS_PIXELS, ROBOT_RADIUS_PIXELS / 2);

        blueRobotImg = new BufferedImage(ROBOT_RADIUS_PIXELS * 2, ROBOT_RADIUS_PIXELS * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics blueRobotGraphics = blueRobotImg.getGraphics();
        blueRobotGraphics.setColor(Color.BLUE);
        blueRobotGraphics.fillOval(0, 0, ROBOT_RADIUS_PIXELS * 2, ROBOT_RADIUS_PIXELS * 2);
        blueRobotGraphics.setColor(Color.MAGENTA);
        blueRobotGraphics.fillOval(ROBOT_RADIUS_PIXELS / 2, 0, ROBOT_RADIUS_PIXELS, ROBOT_RADIUS_PIXELS / 2);

        ballImg = new BufferedImage(BALL_RADIUS_PIXELS * 2, BALL_RADIUS_PIXELS * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics ballGraphics = ballImg.getGraphics();
        ballGraphics.setColor(Color.CYAN);
        ballGraphics.fillOval(0, 0, BALL_RADIUS_PIXELS * 2, BALL_RADIUS_PIXELS * 2);
    }

    public int[] convert(Vec2D v) {
        int[] res = { (int) (v.x * SCALE + windowWidth / 2), (int) (-v.y * SCALE + windowHeight / 2) };
        return res;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        try {
            field.lineSegments.forEach((name, line) -> {
                int[] p1 = convert(line.p1);
                int[] p2 = convert(line.p2);
                g2d.setColor(Color.WHITE);
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
                //g2d.rotate(orient);
                g2d.drawImage(yellowRobotImg, pos[0] - ROBOT_RADIUS_PIXELS, pos[1] - ROBOT_RADIUS_PIXELS, this);
                //g2d.rotate(-orient);
                g2d.setColor(Color.WHITE);
                g2d.drawString(Integer.toString(i), pos[0], pos[1] - ROBOT_RADIUS_PIXELS * 2);
            }
            
            for (int i = 0; i < 6; i++) {
                int[] pos = convert(DetectionData.get().getRobotPos(Team.BLUE, i));
                //double orient = DetectionData.get().getRobotOrient(Team.BLUE, i);
                //g2d.rotate(orient);
                g2d.drawImage(blueRobotImg, pos[0] - ROBOT_RADIUS_PIXELS, pos[1] - ROBOT_RADIUS_PIXELS, this);
                //g2d.rotate(-orient);
                g2d.setColor(Color.WHITE);
                g2d.drawString(Integer.toString(i), pos[0], pos[1] - ROBOT_RADIUS_PIXELS * 2);
            }

            int[] ballPos = convert(DetectionData.get().getBallPos());
            g2d.drawImage(ballImg, ballPos[0], ballPos[1], this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}