package triton.periphModules.display;

import triton.config.Config;
import triton.config.globalVariblesAndConstants.GvcDisplay;
import triton.config.oldConfigs.ObjectConfig;
import triton.coreModules.ai.pathFinder.jumpPointSearch.JPSPathFinder;
import triton.coreModules.ai.pathFinder.jumpPointSearch.Node;
import triton.misc.math.linearAlgebra.Vec2D;

import java.awt.*;
import java.util.ArrayList;

public class JPSPathfinderDisplay extends Display {

    private final JPSPathFinder JPS;

    /**
     * Construct a display with additional path and obstacles
     */
    public JPSPathfinderDisplay(JPSPathFinder JPS, Config config) {
        super(config);
        this.JPS = JPS;
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

        ArrayList<Vec2D> path = JPS.getPath();
        if (path != null && !path.isEmpty()) {
            for (int i = 0; i < path.size() - 1; i++) {
                int[] pointA = convert.fromPos(path.get(i));
                int[] pointB = convert.fromPos(path.get(i + 1));
                g2d.drawLine(pointA[0], pointA[1], pointB[0], pointB[1]);
            }
        }
    }
}
