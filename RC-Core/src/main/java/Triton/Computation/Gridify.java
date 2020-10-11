package Triton.Computation;

import Triton.Shape.Vec2D;

public class Gridify{

    // pos = ±(ind * grid_size + bias)
    // ind = (±pos - bias) / grid_size  
    private Vec2D grid_size;
    private Vec2D bias;
    private boolean flipX; 
    private boolean flipY;

    public Gridify(Vec2D grid_size, Vec2D bias, boolean flipX, boolean flipY) {
        this.grid_size = grid_size;
        this.bias = bias;
        this.flipX = flipX;
        this.flipY = flipY;
    }

    public int numCols(double worldSizeX) {
        return (int) Math.round(worldSizeX / grid_size.x);
    }

    public int numRows(double worldSizeY) {
        return (int) Math.round(worldSizeY / grid_size.y);
    }

    public int[] fromPos(Vec2D vec) {
        int col = (int) Math.round(((flipX ? -1 : 1) * vec.x - bias.x) / grid_size.x);
        int row = (int) Math.round(((flipY ? -1 : 1) * vec.y - bias.y) / grid_size.y);
        int[] res = {col, row};
        return res;
    }

    public Vec2D fromInd(int[] ind) {
        return fromInd(ind[0], ind[1]);
    }

    public Vec2D fromInd(int col, int row) {
        double x = (flipX ? -1 : 1) * (col * grid_size.x + bias.x);
        double y = (flipY ? -1 : 1) * (row * grid_size.y + bias.y);
        return new Vec2D(x, y);
    }
}
