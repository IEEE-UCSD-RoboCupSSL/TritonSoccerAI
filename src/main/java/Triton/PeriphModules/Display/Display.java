package Triton.PeriphModules.Display;

import Triton.Config.DisplayConfig;
import Triton.Config.ObjectConfig;
import Triton.CoreModules.AI.Estimators.GapFinder;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.Math.Coordinates.Gridify;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.Geometry.Circle2D;
import Triton.Misc.Math.Geometry.Line2D;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Subscriber;
import Triton.PeriphModules.Detection.BallData;
import Triton.PeriphModules.Detection.RobotData;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static Triton.PeriphModules.Display.PaintOption.*;

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
    protected Gridify convert;
    GapFinder gapFinder;
    private HashMap<String, Line2D> fieldLines;
    private ArrayList<PaintOption> paintOptions;
    private int windowWidth;
    private int windowHeight;
    private long lastPaint;

    /* Construct a display with robot, ball, and field */
    public Display() {
        super();

        fieldSizeSub = new FieldSubscriber<>("geometry", "fieldSize");
        fieldLinesSub = new FieldSubscriber<>("geometry", "fieldLines");
        fieldCenterSub = new FieldSubscriber<>("geometry", "fieldCenter");

        yellowRobotSubs = new ArrayList<>();
        blueRobotSubs = new ArrayList<>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            blueRobotSubs.add(new FieldSubscriber<>("detection", Team.BLUE.name() + i));
            yellowRobotSubs.add(new FieldSubscriber<>("detection", Team.YELLOW.name() + i));
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

        while (true) { // no delay needed
            HashMap<String, Integer> fieldSize = fieldSizeSub.getMsg();

            if (fieldSize == null || fieldSize.get("fieldLength") == 0 || fieldSize.get("fieldWidth") == 0
                    || fieldSize.get("fullLength") == 0) {
                continue;
            }

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
     *
     * @param g Graphics object to paint to
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        try {
            Graphics2D g2d = (Graphics2D) g;

            if (paintOptions.contains(PROBABILITY))
                paintProbability(g2d);
            if (paintOptions.contains(PREDICTION))
                paintPrediction(g2d);
            if (paintOptions.contains(GEOMETRY))
                paintGeo(g2d);
            if (paintOptions.contains(OBJECTS))
                paintObjects(g2d);
            if (paintOptions.contains(INFO))
                paintInfo(g2d);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Paints the field and lines
     *
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
     *
     * @param g2d Graphics2D object to paint to
     */
    private void paintObjects(Graphics2D g2d) {
        ArrayList<RobotData> robots = new ArrayList<>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            robots.add(yellowRobotSubs.get(i).getMsg());
            robots.add(blueRobotSubs.get(i).getMsg());
        }

        paintRobots(g2d, robots);

        BallData ball = ballSub.getMsg();
        int[] pos = convert.fromPos(ball.getPos());
        g2d.drawImage(ImgLoader.ball, pos[0], pos[1], null);
    }

    private void paintRobots(Graphics2D g2d, ArrayList<RobotData> robots) {
        for (RobotData robot : robots) {
            BufferedImage img;
            if (robot.getTeam() == Team.BLUE)
                img = ImgLoader.blueRobot;
            else
                img = ImgLoader.yellowRobot;

            int[] pos = convert.fromPos(robot.getPos());
            double angle = Math.toRadians(robot.getDir() + 90);

            AffineTransform tx = AffineTransform.getRotateInstance(-angle, img.getWidth() / 2.0,
                    img.getWidth() / 2.0);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

            int imgX = pos[0] - img.getWidth() / 2;
            int imgY = pos[1] - img.getHeight() / 2;
            g2d.drawImage(op.filter(img, null), imgX, imgY, null);
            g2d.setColor(Color.WHITE);
            g2d.drawString(Integer.toString(robot.getID()), pos[0] - 5, pos[1] - 25);
        }
    }

    private void paintProbability(Graphics2D g2d) {
        if (gapFinder == null)
            return;

        HashMap<String, Integer> fieldSize = fieldSizeSub.getMsg();
        double fieldWidth = fieldSize.get("fieldWidth");
        double fieldLength = fieldSize.get("fieldLength");
        double[][] pmf = gapFinder.getPMF();
        for (int x = 0; x < windowWidth; x++) {
            for (int y = 0; y < windowHeight; y++) {
                int[] displayPos = {x, y};
                Vec2D worldPos = convert.fromInd(displayPos);
                double clampedX = Math.max(worldPos.x, -fieldWidth / 2);
                clampedX = Math.min(clampedX, fieldWidth / 2);
                double clampedY = Math.max(worldPos.y, -fieldLength / 2);
                clampedY = Math.min(clampedY, fieldLength / 2);
                Vec2D clampedWorldPos = new Vec2D(clampedX, clampedY);

                double prob = gapFinder.getProb(pmf, clampedWorldPos);

                prob *= 0.8; // to prevent completely white out

                g2d.setColor(new Color((float) prob, (float) prob, (float) prob, 1.0f));
                g2d.fillRect(x, y, 1, 1);
            }
        }
    }

    private void paintPrediction(Graphics2D g2d) {
        BallData ballData = ballSub.getMsg();
        Vec2D pos = ballData.getPos();
        Vec2D vel = ballData.getVel();

        double time = 1;
        Vec2D predPos = pos.add(vel.scale(time));

        int[] screenPos = convert.fromPos(pos);
        int[] screenPredPos = convert.fromPos(predPos);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(5));
        g2d.drawLine(screenPos[0], screenPos[1], screenPredPos[0], screenPredPos[1]);
    }

    /**
     * Paints additional information like FPS
     *
     * @param g2d Graphics2D object to paint to
     */
    private void paintInfo(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);

        g2d.drawString(String.format("LAST UPDATE: %d ms", System.currentTimeMillis() - lastPaint), 50,
                windowHeight - 70);
        g2d.drawString(String.format("FPS: %.1f", 1000.0 / (System.currentTimeMillis() - lastPaint)), 50,
                windowHeight - 50);
    }

    public void setGapFinder(GapFinder gapFinder) {
        this.gapFinder = gapFinder;
    }

    public void setPaintOptions(ArrayList<PaintOption> paintOptions) {
        this.paintOptions = paintOptions;
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