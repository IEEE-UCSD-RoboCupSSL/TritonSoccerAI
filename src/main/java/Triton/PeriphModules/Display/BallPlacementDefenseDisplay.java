package Triton.PeriphModules.Display;

import Triton.Config.Config;
import Triton.CoreModules.AI.AI;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;

import static Triton.PeriphModules.Display.PaintOption.*;

public class BallPlacementDefenseDisplay extends Display {

    private static final int CROSS_RADIUS = 5;
    private Vec2D ballPos = new Vec2D(0, 0);

    public BallPlacementDefenseDisplay(Config config) {
        super(config);

        paintOptions = new ArrayList<>();
        paintOptions.add(GEOMETRY);
        addMouseListener(new DisplayMouseInputAdapter());
    }

    /**
     * Paint the defense formations
     *
     * @param g Graphics object to paint to
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        ArrayList<Vec2D> formation = AI.HandleBallPlacementDefense.getDefenseFormation(config, ballPos).getValue0();
        for (Vec2D point : formation) {
            paintPoint(point, g2d, Color.YELLOW);
        }
        paintPoint(ballPos, g2d, Color.RED);
    }

    /**
     * Paint a point as (default) 5x5 cross;
     * @param point
     */
    public void paintPoint(Vec2D point, Graphics2D g2d, Color color) {
        int[] displayPos = this.convert.fromPos(point);

        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(CROSS_RADIUS / 2.0f));
        g2d.drawLine(displayPos[0] - CROSS_RADIUS, displayPos[1],
                displayPos[0] + CROSS_RADIUS, displayPos[1]);
        g2d.drawLine(displayPos[0], displayPos[1] - CROSS_RADIUS,
                displayPos[0], displayPos[1] + CROSS_RADIUS);
    }

    /**
     * Handles mouse inputs
     */
    private class DisplayMouseInputAdapter extends MouseInputAdapter {
        private final int[] ind = {0, 0};

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                ind[0] = e.getX();
                ind[1] = e.getY();
                ballPos = convert.fromInd(ind);
            }
        }
    }

}
