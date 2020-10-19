package Triton.Computation;

import Triton.Shape.Vec2D;

public class Gridify {

    // pos = ±(ind * grid_size + offset)
    // ind = (±pos - offset) / grid_size  
    private final Vec2D grid_size;
    private final Vec2D offset;
    private final boolean flipX;
    private final boolean flipY;

    public Gridify(Vec2D grid_size, Vec2D offset, boolean flipX, boolean flipY) {
        this.grid_size = grid_size;
        this.offset = offset;
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
        int col = (int) Math.round(((flipX ? -1 : 1) * vec.x - offset.x) / grid_size.x);
        int row = (int) Math.round(((flipY ? -1 : 1) * vec.y - offset.y) / grid_size.y);
        return new int[]{col, row};
    }

    public Vec2D fromInd(int[] ind) {
        return fromInd(ind[0], ind[1]);
    }

    public Vec2D fromInd(int col, int row) {
        double x = (flipX ? -1 : 1) * (col * grid_size.x + offset.x);
        double y = (flipY ? -1 : 1) * (row * grid_size.y + offset.y);
        return new Vec2D(x, y);
    }
}
