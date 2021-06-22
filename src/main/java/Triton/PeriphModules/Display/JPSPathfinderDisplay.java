package Triton.PeriphModules.Display;

import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcDisplay;
import Triton.Config.OldConfigs.ObjectConfig;
import Triton.CoreModules.AI.PathFinder.JumpPointSearch.JPSPathFinder;
import Triton.CoreModules.AI.PathFinder.JumpPointSearch.Node;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.Math.Geometry.Circle2D;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Subscriber;
import Triton.PeriphModules.Detection.BallData;
import Triton.PeriphModules.Detection.RobotData;

import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static Triton.PeriphModules.Display.PaintOption.*;

public class JPSPathfinderDisplay extends Display {

    private final JPSPathFinder JPS;
    private ArrayList<Vec2D> path;

    /**
     * Construct a display with additional path and obstacles
     */
    public JPSPathfinderDisplay(JPSPathFinder JPS, Config config) {
        super(config);

        paintOptions = new ArrayList<>();
        paintOptions.add(GEOMETRY);
        paintOptions.add(OBJECTS);
        paintOptions.add(INFO);
        this.JPS = JPS;

        Timer findPathTimer = new Timer();
        FindPathTask JPSTask = new FindPathTask(this);
        findPathTimer.scheduleAtFixedRate(JPSTask, 0, 20);
        addMouseListener(new DisplayMouseInputAdapter(JPSTask));
    }

    public ArrayList<Vec2D> getPath() {
        return path;
    }

    /**
     * Called to paint the display
     *
     * @param g Graphics object to paint to
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        paintObstacles(g2d);
        paintPath(g2d);
    }

    public void paintObstacles(Graphics2D g2d) {
        if (JPS == null) return;
        for (int col = 0; col < JPS.getNumCols(); col++) {
            for (int row = 0; row < JPS.getNumRows(); row++) {
                Node node = JPS.getNodeList().get(row).get(col);
                Vec2D worldPos = JPS.getConvert().fromInd(node.getX(), node.getY());
                int[] displayPos = this.convert.fromPos(worldPos);
                if (!node.isWalkable()) {
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(5));
                    g2d.drawLine(displayPos[0], displayPos[1], displayPos[0], displayPos[1]);
                }
            }
        }
    }

    /**
     * Paints various pathfinding info for debugging
     *
     * @param g2d Graphics2D object to paint to
     */
    private void paintPath(Graphics2D g2d) {
        if (JPS == null) return;
        g2d.setColor(Color.YELLOW);
        g2d.setStroke(new BasicStroke((int) (ObjectConfig.ROBOT_RADIUS / 2 * GvcDisplay.SCALE)));

        if (path != null && !path.isEmpty()) {
            for (int i = 0; i < path.size() - 1; i++) {
                int[] pointA = convert.fromPos(path.get(i));
                int[] pointB = convert.fromPos(path.get(i + 1));
                g2d.drawLine(pointA[0], pointA[1], pointB[0], pointB[1]);
            }
        }
    }

    private void setPath(ArrayList<Vec2D> path) {
        this.path = path;
    }

    public JPSPathFinder getJPS() {
        return JPS;
    }

    /**
     * Handles mouse inputs
     */
    private class DisplayMouseInputAdapter extends MouseInputAdapter {
        private final FindPathTask findPathTask;
        private final int[] start = {0, 0};
        private final int[] dest = {0, 0};

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


    class FindPathTask extends TimerTask {
        private final JPSPathfinderDisplay display;
        private final ArrayList<Subscriber<RobotData>> yellowRobotSubs;
        private final ArrayList<Subscriber<RobotData>> blueRobotSubs;
        private final Subscriber<BallData> ballSub;

        /* If unspecified, find path from the closest robot to ball */
        private Vec2D start;
        private Vec2D dest;

        public FindPathTask(JPSPathfinderDisplay display) {
            this.display = display;
            yellowRobotSubs = new ArrayList<>();
            blueRobotSubs = new ArrayList<>();

            for (int i = 0; i < config.numAllyRobots; i++) {
                blueRobotSubs.add(new FieldSubscriber<>("From:DetectionModule", Team.BLUE.name() + i));
                yellowRobotSubs.add(new FieldSubscriber<>("From:DetectionModule", Team.YELLOW.name() + i));
            }
            ballSub = new FieldSubscriber<>("From:DetectionModule", "Ball");

            try {
                for (Subscriber<RobotData> robotSub : yellowRobotSubs)
                    robotSub.subscribe(1000);
                for (Subscriber<RobotData> robotSub : blueRobotSubs)
                    robotSub.subscribe(1000);
                ballSub.subscribe(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                ArrayList<RobotData> blueRobots = new ArrayList<>();
                for (int i = 0; i < config.numAllyRobots; i++) {
                    blueRobots.add(blueRobotSubs.get(i).getMsg());
                }

                ArrayList<RobotData> yellowRobots = new ArrayList<>();
                for (int i = 0; i < config.numAllyRobots; i++) {
                    yellowRobots.add(yellowRobotSubs.get(i).getMsg());
                }

                boolean customPath = start != null && dest != null;

                /* Calculate the closest blue robot to the ball */
                RobotData closestRobot = null;
                Vec2D ballPos = ballSub.getMsg().getPos();
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
                ArrayList<Circle2D> obstacles = new ArrayList<>();
                for (int i = 0; i < 6; i++) {
                    RobotData robot = yellowRobots.get(i);
                    obstacles.add(new Circle2D(robot.getPos(), ObjectConfig.ROBOT_RADIUS));
                }
                for (int i = 0; i < 6; i++) {
                    RobotData robot = blueRobots.get(i);
                    if (!customPath && robot == closestRobot) {
                        continue;
                    }
                    obstacles.add(new Circle2D(robot.getPos(), ObjectConfig.ROBOT_RADIUS));
                }

                display.getJPS().setObstacles(obstacles);
                if (customPath) {
                    ArrayList<Vec2D> path = display.getJPS().findPath(start, dest);
                    display.setPath(path);
                } else {
                    display.setPath(display.getJPS().findPath(closestRobotPos, ballPos));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setEnds(Vec2D start, Vec2D dest) {
            this.start = start;
            this.dest = dest;
        }
    }
}
