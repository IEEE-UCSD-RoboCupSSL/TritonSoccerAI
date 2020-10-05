package Triton.Display;

import Triton.Config.ObjectConfig;
import Triton.Computation.ThetaStar.*;
import Triton.Config.DisplayConfig;
import Triton.Detection.*;
import Triton.Detection.RobotData;
import Triton.Shape.*;
import Triton.DesignPattern.PubSubSystem.FieldSubscriber;
import Triton.DesignPattern.PubSubSystem.Subscriber;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;

import java.util.*;
import java.util.Timer;

import javax.swing.*;

import Proto.MessagesRobocupSslGeometry.*;

public class Display extends JPanel {

    private Subscriber<SSL_GeometryFieldSize> fieldSizeSub;
    private Subscriber<HashMap<String, Line2D>> fieldLinesSub;
    private Subscriber<HashMap<Team, HashMap<Integer, RobotData>>> robotSub;
    private Subscriber<BallData> ballSub;

    private SSL_GeometryFieldSize fieldSize;
    private HashMap<String, Line2D> fieldLines;
    private int windowWidth;
    private int windowHeight;

    private JFrame frame;
    private long lastPaint;

    private ArrayList<Vec2D> path;
    private Pathfinder pathfinder;

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

    private class FindPathTask extends TimerTask {
        private Display display;
        private Pathfinder pathfinder;
        private Subscriber<HashMap<Team, HashMap<Integer, RobotData>>> robotSub;
        private Subscriber<BallData> ballSub;

        public FindPathTask(Display display, Pathfinder pathfinder) {
            this.display = display;
            this.pathfinder = pathfinder;
            robotSub = new FieldSubscriber<HashMap<Team, HashMap<Integer, RobotData>>>("detection", "robot");
            ballSub = new FieldSubscriber<BallData>("detection", "ball");
        }

        @Override
        public void run() {
            robotSub.subscribe();
            ballSub.subscribe();
            
            HashMap<Team, HashMap<Integer, RobotData>> robots = robotSub.getMsg();
            BallData ball = ballSub.getMsg();

            RobotData closestRobot = null;
            Vec2D ballPos = ball.getPos();
            double minDist = Double.MAX_VALUE;
            /*
            for (int i = 0; i < 6; i++) {
                RobotData robot = robots.get(Team.YELLOW).get(i);
                Vec2D robotPos = robot.getPos();
                double dist = Vec2D.dist(ballPos, robotPos);
                if (dist < minDist) {
                    closestRobot = robot;
                    minDist = dist;
                }
            }
            */
            for (int i = 0; i < 6; i++) {
                RobotData robot = robots.get(Team.BLUE).get(i);
                Vec2D robotPos = robot.getPos();
                double dist = Vec2D.dist(ballPos, robotPos);
                if (dist < minDist) {
                    closestRobot = robot;
                    minDist = dist;
                }
            }

            Vec2D closestRobotPos = closestRobot.getPos();

            ArrayList<Circle2D> obstacles = new ArrayList<Circle2D>();
            for (int i = 0; i < 6; i++) {
                RobotData robot = robots.get(Team.YELLOW).get(i);
                if (robot == closestRobot) {
                    continue;
                }
                obstacles.add(new Circle2D(robot.getPos(), ObjectConfig.ROBOT_RADIUS));
            }
            for (int i = 0; i < 6; i++) {
                RobotData robot = robots.get(Team.BLUE).get(i);
                if (robot == closestRobot) {
                    continue;
                }
                obstacles.add(new Circle2D(robot.getPos(), ObjectConfig.ROBOT_RADIUS));
            }

            pathfinder.updateGrid(obstacles);
            display.setPath(pathfinder.findPath(closestRobotPos, ballPos));
        }
    }

    public Display() {
        super();

        fieldSizeSub = new FieldSubscriber<SSL_GeometryFieldSize>("geometry", "fieldSize");
        fieldLinesSub = new FieldSubscriber<HashMap<String, Line2D>>("geometry", "fieldLines");
        robotSub = new FieldSubscriber<HashMap<Team, HashMap<Integer, RobotData>>>("detection", "robot");
        ballSub = new FieldSubscriber<BallData>("detection", "ball");

        ImgLoader.loadImages();

        //addMouseListener(new DisplayMouseInputAdapter());

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

            if (fieldSize == null || fieldSize.getFieldLength() == 0 || fieldSize.getFieldWidth() == 0 || fieldSize.getGoalDepth() == 0)
                continue;

            windowWidth = (int) ((fieldSize.getFieldLength() + fieldSize.getGoalDepth() * 2.0) * DisplayConfig.SCALE);
            windowHeight = (int) (fieldSize.getFieldWidth() * DisplayConfig.SCALE);
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

        Timer findPathTimer = new Timer();
        double worldSizeX = fieldSize.getFieldLength();
        double worldSizeY = fieldSize.getFieldWidth();
        pathfinder = new Pathfinder(worldSizeX, worldSizeY);
        FindPathTask findPathTask = new FindPathTask(this, pathfinder);
        findPathTimer.scheduleAtFixedRate(findPathTask, 0, DisplayConfig.UPDATE_DELAY);
    }

    public int[] worldPosToDisplayPos(Vec2D v) {
        int[] res = { (int) Math.round(v.x * DisplayConfig.SCALE + windowWidth / 2),
                (int) Math.round(-v.y * DisplayConfig.SCALE + windowHeight / 2) };
        return res;
    }

    public Vec2D displayPosToWorldPos(int[] v) {
        double x = ((double) v[0] - windowWidth / 2) / DisplayConfig.SCALE;
        double y = -((double) v[1] - windowHeight / 2) / DisplayConfig.SCALE;
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
        fieldLines.forEach((name, line) -> {
            if (name.equals("CenterLine"))
                return;
            int[] p1 = worldPosToDisplayPos(line.p1);
            int[] p2 = worldPosToDisplayPos(line.p2);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(p1[0], p1[1], p2[0], p2[1]);
        });

        for (SSL_FieldCicularArc arc : fieldSize.getFieldArcsList()) {
            int[] center = worldPosToDisplayPos(new Vec2D(arc.getCenter().getX(), arc.getCenter().getY()));
            int radius = (int) (arc.getRadius() * DisplayConfig.SCALE);

            g2d.drawArc(center[0] - radius, center[1] - radius, radius * 2, radius * 2,
                    (int) Math.toDegrees(arc.getA1()), (int) Math.toDegrees(arc.getA2()));
        }
    }

    private void paintObjects(Graphics2D g2d) {
        if (!robotSub.subscribe() || !ballSub.subscribe())
            return;

        HashMap<Team, HashMap<Integer, RobotData>> robots = robotSub.getMsg();

        for (RobotData robot : robots.get(Team.YELLOW).values()) {
            int[] pos = worldPosToDisplayPos(robot.getPos());
            double orient = robot.getOrient();
            AffineTransform tx = AffineTransform.getRotateInstance(orient, ImgLoader.yellowRobot.getWidth() / 2,
                    ImgLoader.yellowRobot.getWidth() / 2);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

            int imgX = pos[0] - ImgLoader.yellowRobot.getWidth() / 2;
            int imgY = pos[1] - ImgLoader.yellowRobot.getHeight() / 2;
            g2d.drawImage(op.filter(ImgLoader.yellowRobot, null), imgX, imgY, null);
            g2d.setColor(Color.WHITE);
            g2d.drawString(Integer.toString(robot.getID()), pos[0] - 5, pos[1] - 25);
        }

        for (RobotData robot : robots.get(Team.BLUE).values()) {
            int[] pos = worldPosToDisplayPos(robot.getPos());
            double orient = robot.getOrient();
            AffineTransform tx = AffineTransform.getRotateInstance(orient, ImgLoader.blueRobot.getWidth() / 2,
                    ImgLoader.blueRobot.getWidth() / 2);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

            int imgX = pos[0] - ImgLoader.blueRobot.getWidth() / 2;
            int imgY = pos[1] - ImgLoader.blueRobot.getHeight() / 2;
            g2d.drawImage(op.filter(ImgLoader.blueRobot, null), imgX, imgY, null);
            g2d.setColor(Color.WHITE);
            g2d.drawString(Integer.toString(robot.getID()), pos[0] - 5, pos[1] - 25);
        }

        BallData ball = ballSub.getMsg();
        int[] ballPos = worldPosToDisplayPos(ball.getPos());
        g2d.drawImage(ImgLoader.ball, ballPos[0], ballPos[1], null);
    }

    private void paintPath(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        g2d.setStroke(new BasicStroke((int) (ObjectConfig.ROBOT_RADIUS / 2 * DisplayConfig.SCALE)));

        if (path != null && !path.isEmpty()) {
            for (int i = 0; i < path.size() - 1; i++) {
                int[] pointA = worldPosToDisplayPos(path.get(i));
                int[] pointB = worldPosToDisplayPos(path.get(i + 1));
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