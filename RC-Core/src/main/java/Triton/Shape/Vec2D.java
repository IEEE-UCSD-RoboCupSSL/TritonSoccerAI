package Triton.Shape;

public class Vec2D {
    public double x,y; 
    private String name;

    public Vec2D(double x, double y) {
        this.x = x;
        this.y = y;   
    }
    
    public Vec2D subtract(Vec2D toSubtract) {
        return new Vec2D(this.x - toSubtract.x, this.y - toSubtract.y);
    }

    public Vec2D multiply(double z) {
        return new Vec2D(this.x * z, this.y * z);
    }

    void setName(String name) {
        this.name = name;
    }

    String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        String s = "";
        s += "<" + x + ", " + y + ">";
        return s;
    }
}