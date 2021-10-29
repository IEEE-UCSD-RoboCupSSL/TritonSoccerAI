package triton.periphModules.display;

import triton.config.Config;
import triton.config.globalVariblesAndConstants.GvcDisplay;
import triton.coreModules.ai.estimators.ProbMapModule;
import triton.coreModules.robot.Team;
import triton.misc.math.coordinates.Gridify;
import triton.misc.math.coordinates.PerspectiveConverter;
import triton.misc.math.geometry.Drawable2D;
import triton.misc.math.linearAlgebra.Vec2D;
import triton.misc.modulePubSubSystem.FieldPubSubPair;
import triton.misc.modulePubSubSystem.FieldSubscriber;
import triton.misc.modulePubSubSystem.Subscriber;
import triton.periphModules.detection.BallData;
import triton.periphModules.detection.RobotData;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static triton.config.globalVariblesAndConstants.GvcGeometry.*;
import static triton.periphModules.display.PaintOption.*;

/**
 * Display to convey information in separate window
 */
public class Display extends JPanel implements Runnable {
    private final ArrayList<Subscriber<RobotData>> yellowRobotSubs;
    private final ArrayList<Subscriber<RobotData>> blueRobotSubs;
    private final Subscriber<BallData> ballSub;
    private final FieldPubSubPair<ArrayList<Drawable2D>> drawablesPubSub;
    private final JFrame frame;
    protected Gridify convert;
    ProbMapModule probMapModule;
    private ArrayList<PaintOption> paintOptions;
    private int windowWidth;
    private int windowHeight;
    private long lastPaint;
    private Config config;

    /* Construct a display with robot, ball, and field */
    public Display(Config config) {
        super();
        this.config = config;

        yellowRobotSubs = new ArrayList<>();
        blueRobotSubs = new ArrayList<>();
        for (int i = 0; i < config.numAllyRobots; i++) {
            blueRobotSubs.add(new FieldSubscriber<>("From:DetectionModule", Team.BLUE.name() + i));
            yellowRobotSubs.add(new FieldSubscriber<>("From:DetectionModule", Team.YELLOW.name() + i));
        }
        ballSub = new FieldSubscriber<>("From:DetectionModule", "Ball");

        drawablesPubSub = new FieldPubSubPair<>("[Pair]DefinedIn:Display", "Drawables", new ArrayList<>());

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

        convert = new Gridify(new Vec2D(1 / GvcDisplay.SCALE, 1 / GvcDisplay.SCALE),
                new Vec2D(-FIELD_WIDTH / 2.0, -FULL_FIELD_LENGTH / 2.0), false, true);
        windowWidth = convert.numCols(FIELD_WIDTH);
        windowHeight = convert.numRows(FULL_FIELD_LENGTH);

        Dimension dimension = new Dimension(windowWidth, windowHeight);
        setPreferredSize(dimension);
        setMinimumSize(dimension);
        setMaximumSize(dimension);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Subscribe to publishers
     */
    private void subscribe() {
        try {
            for (Subscriber<RobotData> robotSub : yellowRobotSubs)
                robotSub.subscribe(1000);
            for (Subscriber<RobotData> robotSub : blueRobotSubs)
                robotSub.subscribe(1000);
            ballSub.subscribe(1000);
            drawablesPubSub.sub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        paintImmediately(0, 0, windowWidth, windowHeight);
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
                paintProbFinder(g2d);
            if (paintOptions.contains(PREDICTION))
                paintPrediction(g2d);
            if (paintOptions.contains(GEOMETRY))
                paintGeo(g2d);
            if (paintOptions.contains(OBJECTS))
                paintObjects(g2d);
            if (paintOptions.contains(INFO))
                paintInfo(g2d);
            if (paintOptions.contains(DRAWABLES))
                paintDrawables(g2d);
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
        FIELD_LINES.forEach((name, line) -> {
            if (name.equals("CenterLine"))
                return;
            int[] p1 = convert.fromPos(PerspectiveConverter.audienceToPlayer(line.p1));
            int[] p2 = convert.fromPos(PerspectiveConverter.audienceToPlayer(line.p2));
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(p1[0], p1[1], p2[0], p2[1]);
        });

        int[] centerPos = convert.fromPos(new Vec2D(FIELD_CIRCLE_CENTER.x, FIELD_CIRCLE_CENTER.y));
        int centerRadius = (int) (FIELD_CIRCLE_RADIUS * GvcDisplay.SCALE);

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
        for (int i = 0; i < config.numAllyRobots; i++) {
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

    public void setProbFinder(ProbMapModule probMapModule) {
        this.probMapModule = probMapModule;
    }

    private void paintProbFinder(Graphics2D g2d) {
        if (probMapModule == null)
            return;

        double[][] pmf = probMapModule.getPMF();
        if(pmf == null) return;

        for (int x = 0; x < windowWidth; x++) {
            for (int y = 0; y < windowHeight; y++) {
                int[] displayPos = {x, y};
                Vec2D worldPos = convert.fromInd(displayPos);
                double clampedX = Math.max(worldPos.x, -FIELD_WIDTH / 2);
                clampedX = Math.min(clampedX, FIELD_WIDTH / 2);
                double clampedY = Math.max(worldPos.y, -FIELD_LENGTH / 2);
                clampedY = Math.min(clampedY, FIELD_LENGTH / 2);
                Vec2D clampedWorldPos = new Vec2D(clampedX, clampedY);

                double prob = probMapModule.getProb(pmf, clampedWorldPos);

                prob *= 0.8; // to prevent completely white out

                g2d.setColor(new Color((float) prob, (float) prob, (float) prob, 1.0f));
                g2d.fillRect(x, y, 1, 1);
            }
        }


        ArrayList<Vec2D> topMaxPos = probMapModule.getTopNMaxPosWithClearance(4, 600);

        // System.out.println(topMaxPos);

        for(Vec2D maxPos : topMaxPos) {
            double clampedX = Math.max(maxPos.x, -FIELD_WIDTH / 2);
            clampedX = Math.min(clampedX, FIELD_WIDTH / 2);
            double clampedY = Math.max(maxPos.y, -FIELD_LENGTH / 2);
            clampedY = Math.min(clampedY, FIELD_LENGTH / 2);
            Vec2D clampedPos = new Vec2D(clampedX, clampedY);
            int[] displayPos = convert.fromPos(clampedPos);
            g2d.setColor(new Color(0, 0.5F, 0.9F));
            g2d.fillRect(displayPos[0], displayPos[1], 10, 10);
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

    private void paintDrawables(Graphics2D g2d) {
        for (Drawable2D drawable : drawablesPubSub.getSub().getMsg()) {
            drawable.draw(g2d, convert);
        }
    }

    public void setPaintOptions(ArrayList<PaintOption> paintOptions) {
        this.paintOptions = paintOptions;
    }
}