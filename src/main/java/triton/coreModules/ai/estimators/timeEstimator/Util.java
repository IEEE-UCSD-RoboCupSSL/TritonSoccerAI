package triton.coreModules.ai.estimators.timeEstimator;

import org.ejml.simple.SimpleMatrix;

import java.util.function.Function;

public class Util {

    private static final double PRECISION = 0.01;

    /**
     * Convert a double array to a N*1 double vector
     */
    public static SimpleMatrix vector(double[] arr) {
        double[][] douArr = new double[1][arr.length];
        douArr[0] = arr;
        return new SimpleMatrix(douArr);
    }

    /**
     * Finding minimizer using gradient descent method
     *
     * @param f  objective function
     * @param x0 the initial x
     * @return minimizer
     */
    public static double gradDescent(Function<Double, Double> f, double x0) {
        double stepCoef = 0.001;
        double prevStep = Double.MAX_VALUE;

        double prevX = x0;
        double prevY = f.apply(prevX);
        double currX = x0 + stepCoef;
        double currY;

        int iter = 100;
        while (prevStep > PRECISION && iter > 0) {
            iter--;
            currY = f.apply(currX);
            if (currY > prevY) {
                stepCoef = -stepCoef / 2; // turn the descent direction
            }
            prevX = currX;
            currX += stepCoef * prevY;
            prevY = currY;
            prevStep = Math.abs(currX - prevX);
        }

        return currX;
    }

    /**
     * Perform bisection method to find the root
     *
     * @param f objective function
     * @param a f(a) and f(b) have different signs
     * @param b ...
     * @return root
     */
    public static double bisection(Function<Double, Double> f, double a, double b) {
        double c = b;
        while ((b - a) >= PRECISION) {
            // Find middle point
            c = (a + b) / 2;
            // Check if middle point is root
            if (f.apply(c) == 0.0) {
                break;
            } else if (f.apply(c) * f.apply(a) < 0) {
                // Decide the side to repeat the steps
                b = c;
            } else {
                a = c;
            }
        }
        return c;
    }
}
