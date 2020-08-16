package Triton.Display;

import Triton.Computation.Pathing;
import Triton.Detection.*;
import Triton.Geometry.*;
import Triton.Shape.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.util.*;
import java.util.Timer;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import Proto.MessagesRobocupSslGeometry.SSL_FieldCicularArc;

public class Display extends JPanel {
    private static final double SCALE = 1.0 / 10.0;

    private static final int TARGET_FPS = 60;
    private static final long UPDATE_DELAY = 1000 / TARGET_FPS; // ms

    private static Field field;
    private ArrayList<Vec2D> points;

    private JFrame frame;
    private static int windowWidth;
    private static int windowHeight;

    private long lastPaint;
    private int[] start = { 0, 0 };
    private int[] des = { 0, 0 };

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

    private class DisplayMouseInputAdapter extends MouseInputAdapter {
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                start[0] = e.getX();
                start[1] = e.getY();
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                des[0] = e.getX();
                des[1] = e.getY();
            }

            ArrayList<Shape2D> obstacles = new ArrayList<Shape2D>();
            for (int i = 0; i < 6; i++) {
                Vec2D pos = DetectionData.get().getRobotPos(Team.YELLOW, i);
                Circle2D obstacle = new Circle2D(pos, ObjectParams.ROBOT_RADIUS);
                obstacles.add(obstacle);
            }
            for (int i = 0; i < 6; i++) {
                Vec2D pos = DetectionData.get().getRobotPos(Team.BLUE, i);
                Circle2D obstacle = new Circle2D(pos, ObjectParams.ROBOT_RADIUS);
                obstacles.add(obstacle);
            }
            points = Pathing.computePathVectorField(convert(start), convert(des), obstacles);
        }
    }

    public Display() {
        super();
        ImgLoader.loadImages();

        addMouseListener(new DisplayMouseInputAdapter());

        Box box = new Box(BoxLayout.Y_AXIS);
        box.add(Box.createVerticalGlue());
        box.add(this);
        box.add(Box.createVerticalGlue());

        frame = new JFrame("Display");
        frame.add(box);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setBackground(Color.decode("#153131"));
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

        Dimension dimension = new Dimension(windowWidth, windowHeight);
        setPreferredSize(dimension);
        setMinimumSize(dimension);
        setMaximumSize(dimension);
        frame.pack();
        frame.setVisible(true);

        Timer repaintTimer = new Timer();
        repaintTimer.scheduleAtFixedRate(new RepaintTask(this), 0, UPDATE_DELAY);
    }

    public int[] convert(Vec2D v) {
        int[] res = { (int) Math.round(v.x * SCALE + windowWidth / 2),
                (int) Math.round(-v.y * SCALE + windowHeight / 2) };
        return res;
    }

    public Vec2D convert(int[] v) {
        double x = ((double) v[0] - windowWidth / 2) / SCALE;
        double y = ((double) v[1] - windowHeight / 2) / -SCALE;
        return new Vec2D(x, y);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        paintGeo(g2d);
        paintObjects(g2d);
        paintPath(g2d);
        paintInfo(g2d);

        lastPaint = System.currentTimeMillis();
    }

    private void paintGeo(Graphics2D g2d) {
        field.lineSegments.forEach((name, line) -> {
            if (name.equals("CenterLine"))
                return;
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
    }

    private void paintObjects(Graphics2D g2d) {
        for (int i = 0; i < 6; i++) {
            int[] pos = convert(DetectionData.get().getRobotPos(Team.YELLOW, i));
            double orient = DetectionData.get().getRobotOrient(Team.YELLOW, i);
            AffineTransform tx = AffineTransform.getRotateInstance(orient, ImgLoader.yellowRobot.getWidth() / 2,
                    ImgLoader.yellowRobot.getWidth() / 2);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
            int imgX = pos[0] - ImgLoader.yellowRobot.getWidth() / 2;
            int imgY = pos[1] - ImgLoader.yellowRobot.getHeight() / 2;
            g2d.drawImage(op.filter(ImgLoader.yellowRobot, null), imgX, imgY, null);
            g2d.setColor(Color.WHITE);
            g2d.drawString(Integer.toString(i), pos[0] - 5, pos[1] - 25);
        }

        for (int i = 0; i < 6; i++) {
            int[] pos = convert(DetectionData.get().getRobotPos(Team.BLUE, i));
            double orient = DetectionData.get().getRobotOrient(Team.BLUE, i);
            AffineTransform tx = AffineTransform.getRotateInstance(orient, ImgLoader.blueRobot.getWidth() / 2,
                    ImgLoader.blueRobot.getWidth() / 2);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
            int imgX = pos[0] - ImgLoader.blueRobot.getWidth() / 2;
            int imgY = pos[1] - ImgLoader.blueRobot.getHeight() / 2;
            g2d.drawImage(op.filter(ImgLoader.blueRobot, null), imgX, imgY, null);
            g2d.setColor(Color.WHITE);
            g2d.drawString(Integer.toString(i), pos[0] - 5, pos[1] - 25);
        }

        int[] ballPos = convert(DetectionData.get().getBallPos());
        g2d.drawImage(ImgLoader.ball, ballPos[0], ballPos[1], null);
    }

    private void paintPath(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        g2d.setStroke(new BasicStroke((int) (ObjectParams.ROBOT_RADIUS * DisplayParams.SCALE)));

        if (points != null) {
            for (int i = 0; i < points.size() - 1; i++) {
                int[] pointA = convert(points.get(i));
                int[] pointB = convert(points.get(i + 1));
                g2d.drawLine(pointA[0], pointA[1], pointB[0], pointB[1]);
            }
        }

        int startImgX = start[0] - ImgLoader.startPoint.getWidth() / 2;
        int startImgY = start[1] - ImgLoader.startPoint.getHeight() / 2;
        g2d.drawImage(ImgLoader.startPoint, startImgX, startImgY, null);

        int desImgX = des[0] - ImgLoader.desPoint.getWidth() / 2;
        int desImgY= des[1] - ImgLoader.desPoint.getHeight() / 2;
        g2d.drawImage(ImgLoader.desPoint, desImgX, desImgY, null);
    }

    private void paintInfo(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);

        g2d.drawString(String.format("START POS: (%d, %d)", start[0], start[1]), 50, 50);
        g2d.drawString(String.format("DES POS: (%d, %d)", des[0], des[1]), 50, 70);

        g2d.drawString(String.format("LAST UPDATE: %d ms", System.currentTimeMillis() - lastPaint), 50,
                windowHeight - 70);
        g2d.drawString(String.format("FPS: %.1f", 1000.0 / (System.currentTimeMillis() - lastPaint)), 50,
                windowHeight - 50);
    }

}