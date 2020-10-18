package Triton.Computation.JPS;

/**
 * @author Kevin
 */
public class Node {
    int x;
    int y;

    boolean walkable = true;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void setWalkable(boolean walkable) { this.walkable = walkable; }
    public boolean isWalkable() { return walkable; }
}
