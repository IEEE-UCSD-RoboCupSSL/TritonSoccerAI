package Triton.Misc.Math.Geometry;

import Triton.Misc.Math.Coordinates.Gridify;

import java.awt.*;

public interface Drawable2D {
    void draw(Graphics2D g2d, Gridify convert);
}
