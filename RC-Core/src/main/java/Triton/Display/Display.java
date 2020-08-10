package Triton.Display;

import Triton.Detection.*;
import Triton.Geometry.*;
import Triton.Shape.*;

import java.awt.*;
import javax.swing.*;
import Proto.MessagesRobocupSslGeometry.SSL_FieldCicularArc;

public class Display extends JPanel implements Runnable {
    private static final double ROBOT_RADIUS = 90;
    private static final double SCALE = 1.0 / 10.0;
    private static final int TARGET_FPS = 15;
    private static final long UPDATE_DELAY = 1000 / TARGET_FPS; // ms

    private static int windowWidth;
    private static int windowHeight;
    private static Field field;

    JFrame frame;

    public Display() {
        super();
        frame = new JFrame("Display");
        frame.add(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.decode("#234823"));
    }

    public void run() {
        while (true) {
            try {
                field = GeometryData.get().getField();
                windowWidth = (int) ((field.fieldLength + field.goalDepth * 2.0) * SCALE);
                windowHeight = (int) ((field.fieldWidth + field.goalDepth * 2.0) * SCALE);
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
                int radius = (int) (ROBOT_RADIUS * SCALE);
                g2d.setColor(Color.YELLOW);
                g2d.fillOval(pos[0] - radius, pos[1] - radius, radius * 2, radius * 2);
                g2d.setColor(Color.BLACK);
                g2d.drawString(Integer.toString(i), pos[0], pos[1]);
            }

            for (int i = 0; i < 6; i++) {
                int[] pos = convert(DetectionData.get().getRobotPos(Team.BLUE, i));
                int radius = (int) (ROBOT_RADIUS * SCALE);
                g2d.setColor(Color.BLUE);
                g2d.fillOval(pos[0] - radius, pos[1] - radius, radius * 2, radius * 2);
                g2d.setColor(Color.WHITE);
                g2d.drawString(Integer.toString(i), pos[0], pos[1]);
            }
        } catch (Exception e) {

        }
    }
}