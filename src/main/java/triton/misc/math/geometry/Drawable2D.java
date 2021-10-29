package triton.misc.math.geometry;

import triton.misc.math.coordinates.Gridify;

import java.awt.*;

public interface Drawable2D {
    void draw(Graphics2D g2d, Gridify convert);
}
