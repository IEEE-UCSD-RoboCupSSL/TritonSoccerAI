package triton.misc.math.linearAlgebra;

import org.ejml.simple.SimpleMatrix;

public class Mat2D {

    private final SimpleMatrix mat;

    public Mat2D(double[][] rowMat) {
        this.mat = new SimpleMatrix(rowMat);
    }

    public Mat2D(SimpleMatrix mat) {
        this.mat = mat;
    }

    /* counter clock wise */
    public static Mat2D rotation(double angleDegree) {
        double theta = Math.toRadians(angleDegree);
        return new Mat2D(new double[][]{
                new double[]{Math.cos(theta), -Math.sin(theta)},
                new double[]{Math.sin(theta), Math.cos(theta)}
        });
    }

    public Mat2D mult(Mat2D mat2) {
        return new Mat2D(mat.mult(mat2.getEJMLmat()));
    }

    public SimpleMatrix getEJMLmat() {
        return this.mat;
    }

    public Vec2D mult(Vec2D vec2) {
        SimpleMatrix rtn = new Mat2D(mat.mult(vec2.toEJML())).getEJMLmat();
        return new Vec2D(rtn.get(0, 0), rtn.get(0, 1));
    }

    public Mat2D add(Mat2D mat2) {
        return new Mat2D(mat.plus(mat2.getEJMLmat()));
    }

    public Mat2D sub(Mat2D mat2) {
        return new Mat2D(mat.minus(mat2.getEJMLmat()));
    }


}
