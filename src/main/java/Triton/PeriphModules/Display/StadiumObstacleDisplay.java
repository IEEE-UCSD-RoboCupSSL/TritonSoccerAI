package Triton.PeriphModules.Display;

import Triton.Config.Config;
import Triton.CoreModules.AI.PathFinder.JumpPointSearch.JPSPathFinder;
import Triton.CoreModules.AI.PathFinder.JumpPointSearch.Node;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import static Triton.PeriphModules.Display.PaintOption.*;

public class StadiumObstacleDisplay extends Display {

    private final JPSPathFinder JPS;

    /**
     * Construct a display with additional path and obstacles
     */
    public StadiumObstacleDisplay(JPSPathFinder JPS, Config config) {
        super(config);

        paintOptions = new ArrayList<>();
        paintOptions.add(GEOMETRY);
        this.JPS = JPS;

        addMouseListener(new DisplayMouseInputAdapter());
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
     * Handles mouse inputs
     */
    private class DisplayMouseInputAdapter extends MouseInputAdapter {
        private final int[] startInd = {10, 10};
        private final int[] destInd = {10, 10};

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                startInd[0] = e.getX();
                startInd[1] = e.getY();
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                destInd[0] = e.getX();
                destInd[1] = e.getY();
            }
            Vec2D start = convert.fromInd(startInd);
            Vec2D dest = convert.fromInd(destInd);

            JPS.setStadiumObstacles(start, dest, 500);
        }
    }

}
