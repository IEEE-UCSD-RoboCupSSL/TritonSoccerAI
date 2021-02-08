package Triton.Misc.Math.Matrix;

import org.ejml.simple.SimpleMatrix;

public class Mat2D {

    private SimpleMatrix mat;

    public Mat2D(double[][] rowMat) {
        this.mat = new SimpleMatrix(rowMat);
    }

    public Mat2D(SimpleMatrix mat) {
        this.mat = mat;
    }

    public SimpleMatrix getEJMLmat() {
        return this.mat;
    }


    public Mat2D mult(Mat2D mat2) {
        return new Mat2D(mat.mult(mat2.getEJMLmat()));
    }
    public Mat2D mult(Vec2D vec2) {
        return new Mat2D(mat.mult(vec2.toEJML()));
    }


    public Mat2D add(Mat2D mat2) {
        return new Mat2D(mat.plus(mat2.getEJMLmat()));
    }

    public Mat2D sub(Mat2D mat2) {
        return new Mat2D(mat.minus(mat2.getEJMLmat()));
    }

    /* counter clock wise */
    public static Mat2D rotation(double angleDegree) {
        double theta = Math.toRadians(angleDegree);
        return new Mat2D(new double[][]{
                new double[] {Math.cos(theta), -Math.sin(theta)},
                new double[] {Math.sin(theta),  Math.cos(theta)}
        });
    }




}
