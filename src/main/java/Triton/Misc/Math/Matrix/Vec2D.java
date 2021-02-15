package Triton.Misc.Math.Matrix;

import Proto.RemoteAPI;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.Geometry.Line2D;
import org.ejml.simple.SimpleMatrix;

/**
 * Represents a 2D vector
 */
public class Vec2D {
    public double x;
    public double y;
    private String name;

    public Vec2D(double playerAngle) {
        double angle = Math.toRadians(playerAngle + 90);
        x = Math.cos(angle);
        y = Math.sin(angle);
    }

    /**
     * Constructs a deep copy of target vector
     *
     * @param target vector to copy
     */
    public Vec2D(Vec2D target) {
        this(target.x, target.y);
        name = target.name;
    }

    /**
     * Constructs a vector with specified x and y values
     *
     * @param x x value of vector
     * @param y y value of vector
     */
    public Vec2D(double x, double y) {
        this.x = x;
        this.y = y;
    }


    public Vec2D(SimpleMatrix mat) {
        this.x = mat.get(0, 0);
        this.y = mat.get(1, 0);
    }


    /**
     * Returns the distance between two vectors
     *
     * @param v1 the first vector
     * @param v2 the second vector
     * @return the distance between the two vectors
     */
    public static double dist(Vec2D v1, Vec2D v2) {
        double diffX2 = Math.pow((v2.x - v1.x), 2);
        double diffY2 = Math.pow((v2.y - v1.y), 2);
        return Math.pow(diffX2 + diffY2, 0.5);
    }

    /**
     * Returns the distance squared between the two vectors
     *
     * @param v1 the first vector
     * @param v2 the second vector
     * @return the distance squared between the two vectors
     */
    public static double dist2(Vec2D v1, Vec2D v2) {
        double diffX2 = Math.pow((v2.x - v1.x), 2);
        double diffY2 = Math.pow((v2.y - v1.y), 2);
        return diffX2 + diffY2;
    }

    public static double angleDiff(Vec2D v1, Vec2D v2) {
        return Math.atan2(v2.y, v2.x) - Math.atan2(v1.y, v1.x);
    }

    /**
     * Add x and y to current vector
     *
     * @param x x value to add
     * @param y y value to add
     * @return a new vector after addition
     */
    public Vec2D add(double x, double y) {
        return new Vec2D(this.x + x, this.y + y);
    }

    /**
     * @return the name of the vector
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name name of vector to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Rotate vector by specified angle
     *
     * @param angle angle to rotate by
     * @return a new vector after rotation
     */
    public Vec2D rotate(double angle) {
        double newX = Math.cos(angle) * x + -Math.sin(angle) * y;
        double newY = Math.sin(angle) * x + Math.cos(angle) * y;
        return new Vec2D(newX, newY);
    }

    /**
     * @return angle starting from y-axis, positive is counter clockwise, between -180 to 180
     */
    public double toPlayerAngle() {
        double angle = toAngle() - 90;
        return PerspectiveConverter.normAng(angle);
    }

    /**
     * @return angle starting from x-axis, positive is counter clockwise
     */
    public double toAngle() {
        if (Math.abs(x) <= 0.00001 || Math.abs(y) <= 0.00001) {
            return 0;
        }

        return Math.toDegrees(Math.atan2(y, x));
    }

    public SimpleMatrix toEJML() {
        return new SimpleMatrix(new double[][]{
                new double[]{x},
                new double[]{y}
        });
    }

    public double distToLine(Line2D line) {
        Vec2D vecA = line.p2.sub(line.p1).normalized();
        Vec2D vecB = this.sub(line.p1);
        Vec2D perpenPoint = line.p1.add(vecA.scale(vecB.dot(vecA)));
        Vec2D vecC = this.sub(perpenPoint).normalized();
        return Math.abs(vecB.dot(vecC));
    }

    /**
     * Add another vector to current vector
     *
     * @param toAdd vector to add
     * @return a new vector after addition
     */
    public Vec2D add(Vec2D toAdd) {
        return new Vec2D(this.x + toAdd.x, this.y + toAdd.y);
    }

    /**
     * Subtract another vector from current vector
     *
     * @param toSubtract vector to subtract by
     * @return a new vector after subtraction
     */
    public Vec2D sub(Vec2D toSubtract) {
        return new Vec2D(this.x - toSubtract.x, this.y - toSubtract.y);
    }

    /**
     * Multiply current vector by a scalar
     *
     * @param z value to multiply by
     * @return a new vector multiplied by the scalar
     */
    public Vec2D scale(double z) {
        return new Vec2D(this.x * z, this.y * z);
    }

    /**
     * Returns the unit vector (vector with length 1)
     *
     * @return the unit vector
     */
    public Vec2D normalized() {
        double mag = mag();

        if (mag < 0.01) {
            return new Vec2D(0, 0);
        } else {
            return new Vec2D(x / mag, y / mag);
        }
    }

    /**
     * Returns the length of the vector
     *
     * @return the length of the vector
     */
    public double mag() {
        return Math.pow(Math.pow(x, 2) + Math.pow(y, 2), 0.5);
    }

    /* dot product */
    public double dot(Vec2D vec) {
        SimpleMatrix a = new SimpleMatrix(new double[][]{
                new double[]{this.x},
                new double[]{this.y}
        });
        SimpleMatrix b = new SimpleMatrix(new double[][]{
                new double[]{vec.x},
                new double[]{vec.y}
        });

        return a.dot(b);
    }

    public double[] toDoubleArray() {
        return new double[]{this.x, this.y};
    }


    /**
     * Returns current vector as a RemoteAPI Vec2D
     *
     * @return current vector as a RemoteAPI Vec2D
     */
    public RemoteAPI.Vec2D toProto() {
        RemoteAPI.Vec2D.Builder builder = RemoteAPI.Vec2D.newBuilder();
        builder.setX(x);
        builder.setY(y);
        return builder.build();
    }

    /**
     * @return the vector as a string representation
     */
    @Override
    public String toString() {
        String s = "";
        s += "<" + x + ", " + y + ">";
        return s;
    }
}