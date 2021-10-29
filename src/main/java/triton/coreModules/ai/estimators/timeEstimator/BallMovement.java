package triton.coreModules.ai.estimators.timeEstimator;

import org.ejml.simple.SimpleMatrix;

import java.util.function.Function;

public class BallMovement {

    private static final SimpleMatrix PARAM = Util.vector(new double[]{317.27728641, -652.73594375, -52.47000213,
            1246.28027845, -145.80701525, -152.98079373, -456.35983333,
            42.78419844, 11.30341907, 55.88504885, 57.48939017,
            -13.63537183, 25.42496705, -33.2416606, 7.06778388}).transpose();

    private static final double MAX_TIME = 10.0;
    /**
     * A safe way to calculate distance.
     *
     * @param s kickVel
     * @param t ETA
     * @return distance
     */
    public static double calcDist(double s, double t) {
        return Math.min(calcDistFast(s, t), calcMaxDist(s)[0]);
    }

    /**
     * Note: this function is unsafe. If the given time is larger than the time to stop,
     * the distance will be largely overestimated.
     *
     * @param s kickVel
     * @param t ETA
     * @return distance
     */
    public static double calcDistFast(double s, double t) {
        SimpleMatrix poly = Util.vector(new double[]{t, s * t, t * t, s * s * t, s * t * t, t * t * t, s * s * s * t, s * s * t * t,
                s * t * t * t, t * t * t * t, s * s * s * s * t, s * s * s * t * t, s * s * t * t * t, s * t * t * t * t, t * t * t * t * t});
        return poly.mult(PARAM).get(0, 0);
    }

    /**
     * @param s kickVel
     * @param t ETA
     * @return distance gradient w.r.t time
     */
    private static double calcGrad(double s, double t) {
        SimpleMatrix poly = Util.vector(new double[]{1, s, t * 2, s * s, s * t * 2, t * t * 3, s * s * s, s * s * t * 2,
                s * t * t * 3, t * t * t * 4, s * s * s * s, s * s * s * t * 2, s * s * t * t * 3, s * t * t * t * 4, t * t * t * t * 5});
        return poly.mult(PARAM).get(0, 0);
    }

    /**
     * @param s KickVel
     * @return estimated max distance and time for reaching it
     */
    public static double[] calcMaxDist(double s) {
        Function<Double, Double> f = (t) -> calcGrad(s, t);
        double maximizer = Util.gradDescent(f, 0.0);
        return new double[]{calcDistFast(s, maximizer), maximizer};
    }


    /**
     * @param s KickVel
     * @param d Distance
     * @return estimated time for reaching the distance
     */
    public static double calcETA(double s, double d) {
        double[] temp = calcMaxDist(s);
        return calcETAFast(s, d, temp);
    }


    /**
     * @param d Distance
     * @param t ETA
     * @return estimated kickVel
     */
    public static double calcKickVel(double d, double t) {
        Function<Double, Double> f = (s) -> calcDistFast(s, t) - d;
        return Util.bisection(f, 0.0, 10.0);
    }

    public static double calcKickVel(double d) {
        Function<Double, Double> f = (s) -> calcMaxDist(s)[0] - d;
        return Util.bisection(f, 0.0, 10.0);
    }

    /**
     * fast ETA with precomputed max dist and time
     */
    public static double calcETAFast(double s, double d, double[] maxPair) {
        if (d <= 0.0) return 0.0;
        double maxDist = maxPair[0];
        double maxTime = maxPair[1];
        if (d > maxDist) return MAX_TIME;
        Function<Double, Double> f = (t) -> calcDistFast(s, t) - d;
        return Util.bisection(f, 0.0, maxTime);
    }
}
