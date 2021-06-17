package Triton.Misc.Math.LinearAlgebra;

import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.simple.SimpleMatrix;

public class LinearAlgebra {

    public static double zeroEp = 0.00001;

    public static double norm(SimpleMatrix matOrVec) {
        return NormOps_DDRM.fastNormP2(matOrVec.getMatrix());
    }

    public static SimpleMatrix normalize(SimpleMatrix matOrVec) {
        double norm = norm(matOrVec);
        if(Math.abs(norm) < zeroEp) return matOrVec;
        return matOrVec.divide(norm);
    }

}
