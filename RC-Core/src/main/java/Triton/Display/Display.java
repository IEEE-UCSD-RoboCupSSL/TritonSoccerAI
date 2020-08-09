package Triton.Display;

import java.awt.Canvas;
import java.awt.Graphics;
import javax.swing.JFrame;

public class Display extends Canvas {
    JFrame frame;
    Canvas canvas;

    public Display() {
        frame = new JFrame("TEST DRAWING");
        canvas = new Display();
        canvas.setSize(400, 400);
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);
    }

    public void paint(Graphics g) {
       g.fillOval(100, 100, 200, 200); 
    }
}