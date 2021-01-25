package Triton.Misc.Coordinates;

/**
 * Class to convert world coordinates to a grid
 */
public class Gridify {

    // pos = ±(ind * grid_size + offset)
    // ind = (±pos - offset) / grid_size  
    private final Vec2D grid_size;
    private final Vec2D offset;
    private final boolean flipX;
    private final boolean flipY;

    /**
     * Construct a grid with specified size, offset, and whether to mirror x and y values
     *
     * @param grid_size width and height of grid as a vector
     * @param offset    offset to shift by
     * @param flipX     whether to flip x coordinates
     * @param flipY     whether to flip y coordinates
     */
    public Gridify(Vec2D grid_size, Vec2D offset, boolean flipX, boolean flipY) {
        this.grid_size = grid_size;
        this.offset = offset;
        this.flipX = flipX;
        this.flipY = flipY;
    }

    /**
     * Returns the number of columns in the grid
     *
     * @param worldSizeX width of the world
     * @return the number of columns in the grid
     */
    public int numCols(double worldSizeX) {
        return (int) Math.round(worldSizeX / grid_size.x);
    }

    /**
     * Returns the number of rows in the grid
     *
     * @param worldSizeY height of the world
     * @return the number of rows in the grid
     */
    public int numRows(double worldSizeY) {
        return (int) Math.round(worldSizeY / grid_size.y);
    }

    /**
     * Convert from world coordinates to grid indices
     *
     * @param vec world coordinates to convert
     * @return grid indices corresponding to the world coordinates
     */
    public int[] fromPos(Vec2D vec) {
        int col = (int) Math.round(((flipX ? -1 : 1) * vec.x - offset.x) / grid_size.x);
        int row = (int) Math.round(((flipY ? -1 : 1) * vec.y - offset.y) / grid_size.y);
        return new int[]{col, row};
    }

    /**
     * Convert from grid indices to world coordinates
     *
     * @param ind array of indices of element in grid
     * @return Vec2D corresponding to the grid indices
     */
    public Vec2D fromInd(int[] ind) {
        return fromInd(ind[0], ind[1]);
    }

    /**
     * Convert from grid indices to world coordinates
     *
     * @param col column index of element in grid
     * @param row row index of element in grid
     * @return Vec2D corresponding to the grid indices
     */
    public Vec2D fromInd(int col, int row) {
        double x = (flipX ? -1 : 1) * (col * grid_size.x + offset.x);
        double y = (flipY ? -1 : 1) * (row * grid_size.y + offset.y);
        return new Vec2D(x, y);
    }
}
