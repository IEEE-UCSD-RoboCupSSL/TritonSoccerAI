package Triton.ManualTests.MiscTests;

import Triton.Config.Config;
import Triton.ManualTests.TritonTestable;
import Triton.VirtualBot.VirtualBot;
import org.ejml.simple.SimpleMatrix;

public class ConstraintMappingMathTests implements TritonTestable {
    @Override
    public boolean test(Config config) {
        SimpleMatrix vec0 = new SimpleMatrix(new double[][]{new double[]{100, 0, 0}}).transpose();
        SimpleMatrix vec1 = new SimpleMatrix(new double[][]{new double[]{0, 100, 0}}).transpose();
        SimpleMatrix vec2 = new SimpleMatrix(new double[][]{new double[]{0, 0, 100}}).transpose();
        SimpleMatrix vec3 = new SimpleMatrix(new double[][]{new double[]{100, 100, 100}}).transpose();

        SimpleMatrix vec4 = new SimpleMatrix(new double[][]{new double[]{0, 0, 0}}).transpose();
        SimpleMatrix vec5 = new SimpleMatrix(new double[][]{new double[]{-100, 0, 0}}).transpose();
        SimpleMatrix vec6 = new SimpleMatrix(new double[][]{new double[]{0, -100, 0}}).transpose();
        SimpleMatrix vec7 = new SimpleMatrix(new double[][]{new double[]{0, 0, -100}}).transpose();
        SimpleMatrix vec8 = new SimpleMatrix(new double[][]{new double[]{44, 88, 0}}).transpose();
        SimpleMatrix vec9 = new SimpleMatrix(new double[][]{new double[]{50, 25, 0}}).transpose();
//        // #print(xy_constraint(np.array([1, 1]), 100, 100))
//        // #print(100 * np.cos(np.radians(45)))
//        System.out.println(VirtualBot.computeXyConstraintAt(new SimpleMatrix(new double[][]{
//                new double[]{}
//        }), config.botConfig.ho));

        SimpleMatrix cmap0 = VirtualBot.constraintMap(VirtualBot.normalizeBodyVector(vec0),
                config.botConfig.robotMaxVerticalSpeed,
                config.botConfig.robotMaxHorizontalSpeed,
                config.botConfig.robotMaxAngularSpeed);
        System.out.println("[100 0 0] ==> : " + cmap0 + " ===> " + config.botConfig.bodyToWheelTransform.mult(cmap0));

        SimpleMatrix cmap1 = VirtualBot.constraintMap(VirtualBot.normalizeBodyVector(vec1),
                config.botConfig.robotMaxVerticalSpeed,
                config.botConfig.robotMaxHorizontalSpeed,
                config.botConfig.robotMaxAngularSpeed);
        System.out.println("[0 100 0] ==> : " + cmap1 + " ===> " + config.botConfig.bodyToWheelTransform.mult(cmap1));

        SimpleMatrix cmap2 = VirtualBot.constraintMap(VirtualBot.normalizeBodyVector(vec2),
                config.botConfig.robotMaxVerticalSpeed,
                config.botConfig.robotMaxHorizontalSpeed,
                config.botConfig.robotMaxAngularSpeed);
        System.out.println("[0 0 100] ==> : " + cmap2 + " ===> " + config.botConfig.bodyToWheelTransform.mult(cmap2));

        SimpleMatrix cmap3 = VirtualBot.constraintMap(VirtualBot.normalizeBodyVector(vec3),
                config.botConfig.robotMaxVerticalSpeed,
                config.botConfig.robotMaxHorizontalSpeed,
                config.botConfig.robotMaxAngularSpeed);
        System.out.println("[100 100 100] ==> : " + cmap3 + " ===> " + config.botConfig.bodyToWheelTransform.mult(cmap3));

        SimpleMatrix cmap4 = VirtualBot.constraintMap(VirtualBot.normalizeBodyVector(vec4),
                config.botConfig.robotMaxVerticalSpeed,
                config.botConfig.robotMaxHorizontalSpeed,
                config.botConfig.robotMaxAngularSpeed);
        System.out.println("[0 0 0] ==> : " + cmap4 + " ===> " + config.botConfig.bodyToWheelTransform.mult(cmap4));

        SimpleMatrix cmap5 = VirtualBot.constraintMap(VirtualBot.normalizeBodyVector(vec5),
                config.botConfig.robotMaxVerticalSpeed,
                config.botConfig.robotMaxHorizontalSpeed,
                config.botConfig.robotMaxAngularSpeed);
        System.out.println("[-100 0 0] ==> : " + cmap5 + " ===> " + config.botConfig.bodyToWheelTransform.mult(cmap5));

        SimpleMatrix cmap6 = VirtualBot.constraintMap(VirtualBot.normalizeBodyVector(vec6),
                config.botConfig.robotMaxVerticalSpeed,
                config.botConfig.robotMaxHorizontalSpeed,
                config.botConfig.robotMaxAngularSpeed);
        System.out.println("[0 -100 0] ==> : " + cmap6 + " ===> " + config.botConfig.bodyToWheelTransform.mult(cmap6));


        SimpleMatrix cmap7 = VirtualBot.constraintMap(VirtualBot.normalizeBodyVector(vec7),
                config.botConfig.robotMaxVerticalSpeed,
                config.botConfig.robotMaxHorizontalSpeed,
                config.botConfig.robotMaxAngularSpeed);
        System.out.println("[0 0 -100] ==> : " + cmap7 + " ===> " + config.botConfig.bodyToWheelTransform.mult(cmap7));

        SimpleMatrix cmap8 = VirtualBot.constraintMap(VirtualBot.normalizeBodyVector(vec8),
                config.botConfig.robotMaxVerticalSpeed,
                config.botConfig.robotMaxHorizontalSpeed,
                config.botConfig.robotMaxAngularSpeed);
        System.out.println("[44 88 0] ==> : " + cmap8 + " ===> " + config.botConfig.bodyToWheelTransform.mult(cmap8));

        SimpleMatrix cmap9 = VirtualBot.constraintMap(VirtualBot.normalizeBodyVector(vec9),
                config.botConfig.robotMaxVerticalSpeed,
                config.botConfig.robotMaxHorizontalSpeed,
                config.botConfig.robotMaxAngularSpeed);
        System.out.println("[50 25 0] ==> : " + cmap9 + " ===> " + config.botConfig.bodyToWheelTransform.mult(cmap9));



        return true;
    }
}
