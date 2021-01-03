package Triton.Modules.Display;

import Proto.MessagesRobocupSslGeometry.SSL_FieldCicularArc;
import Proto.MessagesRobocupSslGeometry.SSL_GeometryFieldSize;
import Triton.Algorithms.PathFinder.JPS.Node;
import Triton.Dependencies.Gridify;
import Triton.Algorithms.PathFinder.JPS.JPSPathFinder;
import Triton.Config.DisplayConfig;
import Triton.Config.ObjectConfig;
import Triton.Dependencies.DesignPattern.PubSubSystem.FieldSubscriber;
import Triton.Dependencies.DesignPattern.PubSubSystem.Subscriber;
import Triton.Dependencies.PerspectiveConverter;
import Triton.Dependencies.Shape.Circle2D;
import Triton.Modules.Detection.BallData;
import Triton.Modules.Detection.RobotData;
import Triton.Dependencies.Shape.Line2D;
import Triton.Dependencies.Shape.Vec2D;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Display to convey information in separate window
 */
public class Display extends JPanel {

    private final Subscriber<HashMap<String, Integer>> fieldSizeSub;
    private final Subscriber<HashMap<String, Line2D>> fieldLinesSub;
    private final Subscriber<Circle2D> fieldCenterSub;
    private final ArrayList<Subscriber<RobotData>> yellowRobotSubs;
    private final ArrayList<Subscriber<RobotData>> blueRobotSubs;
    private final Subscriber<BallData> ballSub;
    private final JFrame frame;
    private HashMap<String, Line2D> fieldLines;
    private int windowWidth;
    private int windowHeight;
    private long lastPaint;

    protected Gridify convert;


    /* Construct a display with robot, ball, and field */
    public Display() {
        super();

        fieldSizeSub = new FieldSubscriber<>("geometry", "fieldSize");
        fieldLinesSub = new FieldSubscriber<>("geometry", "fieldLines");
        fieldCenterSub = new FieldSubscriber<>("geometry", "fieldCenter");

        yellowRobotSubs = new ArrayList<>();
        blueRobotSubs = new ArrayList<>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobotSubs.add(new FieldSubscriber<>("detection", "yellow robot data" + i));
            blueRobotSubs.add(new FieldSubscriber<>("detection", "blue robot data" + i));
        }
        ballSub = new FieldSubscriber<>("detection", "ball");

        ImgLoader.generateImages();

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

    /**
     * Begin displaying
     */
    public void start() {
        subscribe();

        while (true) {
            HashMap<String, Integer> fieldSize = fieldSizeSub.getMsg();

            if (fieldSize == null || fieldSize.get("fieldLength") == 0 || fieldSize.get("fieldWidth") == 0
                    || fieldSize.get("fullLength") == 0)
                continue;

            int fieldWidth = fieldSize.get("fieldWidth");
            int fieldLength = fieldSize.get("fieldLength");
            int fullLength = fieldSize.get("fullLength");

            convert = new Gridify(new Vec2D(1 / DisplayConfig.SCALE, 1 / DisplayConfig.SCALE),
                    new Vec2D(-fieldWidth / 2.0, -fullLength / 2.0), false, true);

            windowWidth = convert.numCols(fieldWidth);
            windowHeight = convert.numRows(fullLength);
            break;
        }

        do {
            fieldLines = fieldLinesSub.getMsg();
        } while (fieldLines == null);

        Dimension dimension = new Dimension(windowWidth, windowHeight);
        setPreferredSize(dimension);
        setMinimumSize(dimension);
        setMaximumSize(dimension);
        frame.pack();
        frame.setVisible(true);

        Timer repaintTimer = new Timer();
        repaintTimer.scheduleAtFixedRate(new RepaintTask(this), 0, DisplayConfig.UPDATE_DELAY);
    }

    /**
     * Subscribe to publishers
     */
    private void subscribe() {
        try {
            fieldSizeSub.subscribe(1000);
            fieldLinesSub.subscribe(1000);
            fieldCenterSub.subscribe(1000);
            for (Subscriber<RobotData> robotSub : yellowRobotSubs)
                robotSub.subscribe(1000);
            for (Subscriber<RobotData> robotSub : blueRobotSubs)
                robotSub.subscribe(1000);
            ballSub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called to paint the display
     * @param g Graphics object to paint to
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        paintGeo(g2d);
        paintObjects(g2d);
        paintInfo(g2d);

        lastPaint = System.currentTimeMillis();
    }

    /**
     * Paints the field and lines
     * @param g2d Graphics2D object to paint to
     */
    private void paintGeo(Graphics2D g2d) {
        fieldLines.forEach((name, line) -> {
            if (name.equals("CenterLine"))
                return;
            int[] p1 = convert.fromPos(PerspectiveConverter.audienceToPlayer(line.p1));
            int[] p2 = convert.fromPos(PerspectiveConverter.audienceToPlayer(line.p2));
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(p1[0], p1[1], p2[0], p2[1]);
        });

        Circle2D center = fieldCenterSub.getMsg();
        int[] centerPos = convert.fromPos(new Vec2D(center.center.x, center.center.y));
        int centerRadius = (int) (center.radius * DisplayConfig.SCALE);

        g2d.drawArc(centerPos[0] - centerRadius, centerPos[1] - centerRadius, centerRadius * 2,
                centerRadius * 2, 0, 360);
    }

    /**
     * Paints robots and ball
     * @param g2d Graphics2D object to paint to
     */
    private void paintObjects(Graphics2D g2d) {
        ArrayList<RobotData> yellowRobots = new ArrayList<>();
        ArrayList<RobotData> blueRobots = new ArrayList<>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobots.add(yellowRobotSubs.get(i).getMsg());
            blueRobots.add(blueRobotSubs.get(i).getMsg());
        }

        for (RobotData robot : yellowRobots) {
            int[] pos = convert.fromPos(robot.getPos());
            double angle = robot.getAngle() + 180;

            AffineTransform tx = AffineTransform.getRotateInstance(-angle, ImgLoader.yellowRobot.getWidth() / 2.0,
                    ImgLoader.yellowRobot.getWidth() / 2.0);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

            int imgX = pos[0] - ImgLoader.yellowRobot.getWidth() / 2;
            int imgY = pos[1] - ImgLoader.yellowRobot.getHeight() / 2;
            g2d.drawImage(op.filter(ImgLoader.yellowRobot, null), imgX, imgY, null);
            g2d.setColor(Color.WHITE);
            g2d.drawString(Integer.toString(robot.getID()), pos[0] - 5, pos[1] - 25);
        }

        for (RobotData robot : blueRobots) {
            int[] pos = convert.fromPos(robot.getPos());
            double angle = robot.getAngle() + 180;

            AffineTransform tx = AffineTransform.getRotateInstance(-angle, ImgLoader.blueRobot.getWidth() / 2.0,
                    ImgLoader.blueRobot.getWidth() / 2.0);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

            int imgX = pos[0] - ImgLoader.blueRobot.getWidth() / 2;
            int imgY = pos[1] - ImgLoader.blueRobot.getHeight() / 2;
            g2d.drawImage(op.filter(ImgLoader.blueRobot, null), imgX, imgY, null);
            g2d.setColor(Color.WHITE);
            g2d.drawString(Integer.toString(robot.getID()), pos[0] - 5, pos[1] - 25);
        }

        BallData ball = ballSub.getMsg();
        int[] pos = convert.fromPos(ball.getPos());
        g2d.drawImage(ImgLoader.ball, pos[0], pos[1], null);
    }

    /**
     * Paints additional information like FPS
     * @param g2d Graphics2D object to paint to
     */
    private void paintInfo(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);

        g2d.drawString(String.format("LAST UPDATE: %d ms", System.currentTimeMillis() - lastPaint), 50,
                windowHeight - 70);
        g2d.drawString(String.format("FPS: %.1f", 1000.0 / (System.currentTimeMillis() - lastPaint)), 50,
                windowHeight - 50);
    }

    /**
     * Task to call paint at set intervals
     */
    private static class RepaintTask extends TimerTask {
        private final Display display;

        public RepaintTask(Display display) {
            this.display = display;
        }

        @Override
        public void run() {
            display.paintImmediately(0, 0, display.windowWidth, display.windowHeight);
        }
    }
}