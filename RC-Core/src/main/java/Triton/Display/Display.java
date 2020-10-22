package Triton.Display;

import Triton.Computation.JPS.JPSPathFinder;
import Triton.Computation.PathFinder;
import Triton.Config.ObjectConfig;
import Triton.Computation.Gridify;
import Triton.Computation.ThetaStar.*;
import Triton.Config.DisplayConfig;
import Triton.Detection.*;
import Triton.Detection.RobotData;
import Triton.Shape.*;
import Triton.DesignPattern.PubSubSystem.FieldSubscriber;
import Triton.DesignPattern.PubSubSystem.Subscriber;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.event.MouseEvent;
import java.awt.image.*;

import java.util.*;
import java.util.Timer;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import Proto.MessagesRobocupSslGeometry.*;

@SuppressWarnings("serial")
public class Display extends JPanel {

    private Subscriber<SSL_GeometryFieldSize> fieldSizeSub;
    private Subscriber<HashMap<String, Line2D>> fieldLinesSub;
    private ArrayList<Subscriber<RobotData>> yellowRobotSubs;
    private ArrayList<Subscriber<RobotData>> blueRobotSubs;
    private Subscriber<BallData> ballSub;

    private SSL_GeometryFieldSize fieldSize;
    private HashMap<String, Line2D> fieldLines;
    private int windowWidth;
    private int windowHeight;

    private JPSPathFinder JPS;
    private JFrame frame;
    private long lastPaint;

    private ArrayList<Vec2D> path;
    private Gridify convert;

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

    private static class FindPathTask extends TimerTask {
        private Display display;
        private PathFinder pathfinder;
        private ArrayList<Subscriber<RobotData>> yellowRobotSubs;
        private ArrayList<Subscriber<RobotData>> blueRobotSubs;
        private Subscriber<BallData> ballSub;

        /* If unspecified, find path from the closest robot to ball */
        private Vec2D start;
        private Vec2D dest;

        public FindPathTask(Display display, PathFinder pathfinder) {
            this.display = display;
            this.pathfinder = pathfinder;

            yellowRobotSubs = new ArrayList<Subscriber<RobotData>>();
            blueRobotSubs = new ArrayList<Subscriber<RobotData>>();
            for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
                yellowRobotSubs.add(new FieldSubscriber<RobotData>("detection", "yellow robot data" + i));
                blueRobotSubs.add(new FieldSubscriber<RobotData>("detection", "blue robot data" + i));
            }

            ballSub = new FieldSubscriber<BallData>("detection", "ball");
        }

        public void setEnds(Vec2D start, Vec2D dest) {
            this.start = start;
            this.dest = dest;
        }

        @Override
        public void run() {
            for (Subscriber<RobotData> robotSub : yellowRobotSubs) {
                robotSub.subscribe();
            }
            for (Subscriber<RobotData> robotSub : blueRobotSubs) {
                robotSub.subscribe();
            }
            ballSub.subscribe();

            ArrayList<RobotData> blueRobots = new ArrayList<RobotData>();
            for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
                blueRobots.add(blueRobotSubs.get(i).getMsg());
            }

            ArrayList<RobotData> yellowRobots = new ArrayList<RobotData>();
            for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
                yellowRobots.add(yellowRobotSubs.get(i).getMsg());
            }

            BallData ball = ballSub.getMsg();
            boolean customPath = start != null && dest != null;

            /* Calculate the closest blue robot to the ball */
            RobotData closestRobot = null;
            Vec2D ballPos = ball.getPos();
            double minDist = Double.MAX_VALUE;
            for (int i = 0; i < 6; i++) {
                RobotData robot = blueRobots.get(i);
                Vec2D robotPos = robot.getPos();
                double dist = Vec2D.dist2(ballPos, robotPos);
                if (dist < minDist) {
                    closestRobot = robot;
                    minDist = dist;
                }
            }
            Vec2D closestRobotPos = closestRobot.getPos();

            /* Add all (other) robots as obstacles */
            ArrayList<Circle2D> obstacles = new ArrayList<Circle2D>();
            for (int i = 0; i < 6; i++) {
                RobotData robot = yellowRobots.get(i);
                if (!customPath && robot == closestRobot) {
                    continue;
                }
                obstacles.add(new Circle2D(robot.getPos(), ObjectConfig.ROBOT_RADIUS));
            }
            for (int i = 0; i < 6; i++) {
                RobotData robot = blueRobots.get(i);
                if (!customPath && robot == closestRobot) {
                    continue;
                }
                obstacles.add(new Circle2D(robot.getPos(), ObjectConfig.ROBOT_RADIUS));
            }

            pathfinder.setObstacles(obstacles);
            if (customPath) {
                long t0 = System.nanoTime();
                ArrayList<Vec2D> path = pathfinder.findPath(start, dest);
                long t1 = System.nanoTime();
                System.out.println(pathfinder.getName() + " takes " + (t1 - t0) + " ns");

                display.setPath(path);
            } else {
                display.setPath(pathfinder.findPath(closestRobotPos, ballPos));
            }
        }
    }

    private class DisplayMouseInputAdapter extends MouseInputAdapter {
        private final FindPathTask findPathTask;
        private final int[] start = { 0, 0 };
        private final int[] dest = { 0, 0 };

        public DisplayMouseInputAdapter(FindPathTask findPathTask) {
            this.findPathTask = findPathTask;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                start[0] = e.getX();
                start[1] = e.getY();
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                dest[0] = e.getX();
                dest[1] = e.getY();
            }

            findPathTask.setEnds(convert.fromInd(start), convert.fromInd(dest));
            findPathTask.run();
        }
    }

    public Display() {
        super();

        fieldSizeSub = new FieldSubscriber<SSL_GeometryFieldSize>("geometry", "fieldSize");
        fieldLinesSub = new FieldSubscriber<HashMap<String, Line2D>>("geometry", "fieldLines");

        yellowRobotSubs = new ArrayList<Subscriber<RobotData>>();
        blueRobotSubs = new ArrayList<Subscriber<RobotData>>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobotSubs.add(new FieldSubscriber<RobotData>("detection", "yellow robot data" + i));
            blueRobotSubs.add(new FieldSubscriber<RobotData>("detection", "blue robot data" + i));
        }

        new FieldSubscriber<HashMap<Team, HashMap<Integer, RobotData>>>("detection", "robot");

        ballSub = new FieldSubscriber<BallData>("detection", "ball");

        ImgLoader.loadImages();

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
        fieldSizeSub.subscribe();

        while (true) {
            fieldSize = fieldSizeSub.getMsg();

            if (fieldSize == null || fieldSize.getFieldLength() == 0 || fieldSize.getFieldWidth() == 0
                    || fieldSize.getGoalDepth() == 0)
                continue;

            double fullLength = fieldSize.getFieldLength() + fieldSize.getGoalDepth() * 2.0;

            convert = new Gridify(new Vec2D(1 / DisplayConfig.SCALE, 1 / DisplayConfig.SCALE),
                    new Vec2D(-fullLength / 2, -fieldSize.getFieldWidth() / 2.0), false, true);

            windowWidth = convert.numCols(fullLength);
            windowHeight = convert.numRows(fieldSize.getFieldWidth());
            break;
        }

        fieldLinesSub.subscribe();
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

        // Timer findPathTimer = new Timer();
        double worldSizeX = fieldSize.getFieldLength();
        double worldSizeY = fieldSize.getFieldWidth();

        JPS = new JPSPathFinder(worldSizeX, worldSizeY);
        FindPathTask JPSTask = new FindPathTask(this, JPS);

        //PathFinder thetaStar = new ThetaStarPathFinder(worldSizeX, worldSizeY);
        //FindPathTask thetaStarTask = new FindPathTask(this, thetaStar);

        // findPathTimer.scheduleAtFixedRate(findPathTask, 0,
        // DisplayConfig.UPDATE_DELAY);
        //addMouseListener(new DisplayMouseInputAdapter(thetaStarTask));
        addMouseListener(new DisplayMouseInputAdapter(JPSTask));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        paintGeo(g2d);
        paintObjects(g2d);
        if (JPS != null) {
            JPS.paintObstacles(g2d, convert);
        }
        paintPath(g2d);
        paintInfo(g2d);

        lastPaint = System.currentTimeMillis();
    }

    private void paintGeo(Graphics2D g2d) {
        fieldLines.forEach((name, line) -> {
            if (name.equals("CenterLine"))
                return;
            int[] p1 = convert.fromPos(line.p1);
            int[] p2 = convert.fromPos(line.p2);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(p1[0], p1[1], p2[0], p2[1]);
        });

        for (SSL_FieldCicularArc arc : fieldSize.getFieldArcsList()) {
            int[] center = convert.fromPos(new Vec2D(arc.getCenter().getX(), arc.getCenter().getY()));
            int radius = (int) (arc.getRadius() * DisplayConfig.SCALE);

            g2d.drawArc(center[0] - radius, center[1] - radius, radius * 2, radius * 2,
                    (int) Math.toDegrees(arc.getA1()), (int) Math.toDegrees(arc.getA2()));
        }
    }

    private void paintObjects(Graphics2D g2d) {
        for (Subscriber<RobotData> robotSub : yellowRobotSubs) {
            robotSub.subscribe();
        }
        for (Subscriber<RobotData> robotSub : blueRobotSubs) {
            robotSub.subscribe();
        }
        ballSub.subscribe();

        ArrayList<RobotData> yellowRobots = new ArrayList<RobotData>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            yellowRobots.add(yellowRobotSubs.get(i).getMsg());
        }

        ArrayList<RobotData> blueRobots = new ArrayList<RobotData>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            blueRobots.add(blueRobotSubs.get(i).getMsg());
        }

        for (RobotData robot : yellowRobots) {
            int[] pos = convert.fromPos(robot.getPos());
            double orient = robot.getOrient();
            AffineTransform tx = AffineTransform.getRotateInstance(orient, ImgLoader.yellowRobot.getWidth() / 2,
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
            double orient = robot.getOrient();
            AffineTransform tx = AffineTransform.getRotateInstance(orient, ImgLoader.blueRobot.getWidth() / 2,
                    ImgLoader.blueRobot.getWidth() / 2.0);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

            int imgX = pos[0] - ImgLoader.blueRobot.getWidth() / 2;
            int imgY = pos[1] - ImgLoader.blueRobot.getHeight() / 2;
            g2d.drawImage(op.filter(ImgLoader.blueRobot, null), imgX, imgY, null);
            g2d.setColor(Color.WHITE);
            g2d.drawString(Integer.toString(robot.getID()), pos[0] - 5, pos[1] - 25);
        }

        BallData ball = ballSub.getMsg();
        int[] ballPos = convert.fromPos(ball.getPos());
        g2d.drawImage(ImgLoader.ball, ballPos[0], ballPos[1], null);
    }

    private void paintPath(Graphics2D g2d) {
        /*
         * Grid grid = pathfinder.getGrid(); Node[][] nodes = grid.getNodes(); for (int
         * row = 0; row < grid.getNumRows(); row++) { for (int col = 0; col <
         * grid.getNumCols(); col++) { Node node = nodes[row][col]; Vec2D worldPos =
         * node.getWorldPos(); int[] displayPos = convert.fromPos(worldPos); if
         * (!node.getWalkable()) { g2d.setColor(Color.RED); g2d.setStroke(new
         * BasicStroke(5)); g2d.drawLine(displayPos[0], displayPos[1], displayPos[0],
         * displayPos[1]); } } }
         */

        g2d.setColor(Color.YELLOW);
        g2d.setStroke(new BasicStroke((int) (ObjectConfig.ROBOT_RADIUS / 2 * DisplayConfig.SCALE)));

        if (path != null && !path.isEmpty()) {
            for (int i = 0; i < path.size() - 1; i++) {
                int[] pointA = convert.fromPos(path.get(i));
                int[] pointB = convert.fromPos(path.get(i + 1));
                g2d.drawLine(pointA[0], pointA[1], pointB[0], pointB[1]);
            }
        }
    }

    private void paintInfo(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);

        g2d.drawString(String.format("LAST UPDATE: %d ms", System.currentTimeMillis() - lastPaint), 50,
                windowHeight - 70);
        g2d.drawString(String.format("FPS: %.1f", 1000.0 / (System.currentTimeMillis() - lastPaint)), 50,
                windowHeight - 50);
    }

    public void setPath(ArrayList<Vec2D> path) {
        this.path = path;
    }
}