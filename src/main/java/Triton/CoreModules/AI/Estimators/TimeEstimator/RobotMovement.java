package Triton.CoreModules.AI.Estimators.TimeEstimator;

import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import org.ejml.simple.SimpleMatrix;

import java.util.function.Function;

public class RobotMovement {

    /* Parameters for predicting start time */
    private static final SimpleMatrix START_TIME_COEF =
            Util.vector(new double[]{8.68896762e-03, -5.68910669e-05, 1.60672843e-07}).transpose();
    private static final double START_TIME_INTER = -0.08804982055897792;

    /* Parameters for predicting start angle */
    private static final SimpleMatrix START_ANGLE_COEF_1 =
            Util.vector(new double[]{-5.32864705e+00, 4.97455960e-02, -1.46250178e-04}).transpose();
    private static final double START_ANGLE_INTER_1 = 184.18153596594723;
    private static final SimpleMatrix START_ANGLE_COEF_2 =
            Util.vector(new double[]{1.22597077e+00, -1.69369146e-02, 5.83072930e-05}).transpose();
    private static final double START_ANGLE_INTER_2 = -14.862288587036414;
    private static final double REST = 1.1570425183710644;

    /* Parameters for calculating max speed on certain direction */
    private static final double M = 1047.911860892551;
    private static final double N = 1813.5086417317668;

    /* Parameters for calculating robot acceleration and deceleration */
    private static final double K = 1.2626501190020936;
    private static final double W = 0.16288194277448656;

    public static double calcETA(double initial_angle, Vec2D initial_vel, Vec2D dest, Vec2D curr) {
        Vec2D path = dest.sub(curr);
        return calcETA(initial_angle, initial_vel.mag(), path.toPlayerAngle(), dest, curr);
    }

    /**
     * @param initial_angle current robot facing angle, in player perspective
     * @param end_angle     intended final facing angle, in player perspective
     * @param initial_speed initial speed
     * @param dest          destination, in player perspective
     * @param curr          current position, in player perspective
     * @return estimated time
     */
    public static double calcETA(double initial_angle, double initial_speed, double end_angle,
                                 Vec2D dest, Vec2D curr) {
        Vec2D path = dest.sub(curr);
        double total_dist = path.mag();
        double path_angle = path.toPlayerAngle();

        // Predict the start angle
        double initial_angle_diff = PerspectiveConverter.calcAngDiff(end_angle, initial_angle);
        double start_angle_diff = calcStartAngle(initial_angle_diff);
        double rotation_time = calcStartTime(initial_angle_diff);
        boolean clockwise = (end_angle - initial_angle) % 360 != initial_angle_diff;
        double start_angle = end_angle + (clockwise ? start_angle_diff : (-start_angle_diff));

        // Calculate directional max-speed
        double start_speed = calcMaxSpeed(PerspectiveConverter.calcAngDiff(start_angle, path_angle));
        double end_speed = calcMaxSpeed(PerspectiveConverter.calcAngDiff(end_angle, path_angle));

        // Predict the acceleration and deceleration distance and time, assume small angle change
        double acc_dist = Math.pow(start_speed, -K) * Math.pow(start_speed - initial_speed, 2) / (2 * W);
        double acc_time = (start_speed - initial_speed) / Math.pow(start_speed, K) / W;
        double dec_dist = Math.pow(end_speed, 2 - K) / (2 * W);
        double dec_time = end_speed / Math.pow(end_speed, K) / W;

        // Case in which robot cannot accelerate to max_speed
        double temp = acc_dist + dec_dist;
        if (acc_dist + dec_dist > total_dist) {
            Function<Double, Double> f = (v) -> v * v / (2 * W * Math.pow(end_speed, K)) +
                    Math.pow(v - initial_speed, 2) / (2 * W * Math.pow(start_speed, K)) - total_dist;

            double max_speed = Util.bisection(f, initial_speed + 1e-5, Math.max(start_speed, end_speed));
            acc_time = (max_speed - initial_speed) / Math.pow(start_speed, K) / W;
            dec_time = max_speed / Math.pow(end_speed, K) / W;
            return rotation_time + acc_time + dec_time;
        }

        // Robot accelerates to max_speed
        return rotation_time + acc_time + dec_time + (total_dist - temp) / (start_speed + end_speed) * 2;
    }

    /**
     * @param a the angle between initial angle and end angle
     * @return the difference between initial angle and start angle
     */
    public static double calcStartAngle(double a) {
        a = Math.abs(a);
        if (a < 20) return a;
        if (a > 165) return REST; // start angle highly unpredictable when angle diff > 165

        SimpleMatrix coef;
        double inte;
        if (a > 90) { // different model for > 90 and < 90
            coef = START_ANGLE_COEF_1;
            inte = START_ANGLE_INTER_1;
        } else {
            coef = START_ANGLE_COEF_2;
            inte = START_ANGLE_INTER_2;
        }
        return Util.vector(new double[]{a, a * a, a * a * a}).mult(coef).get(0, 0) + inte;
    }

    /**
     * @param a the angle between initial angle and end angle
     * @return the time used for rotating to the start angle
     */
    public static double calcStartTime(double a) {
        a = Math.abs(a);
        if (a < 20 && a > -20) return 0;
        return Util.vector(new double[]{a, a * a, a * a * a})
                .mult(START_TIME_COEF).get(0, 0) + START_TIME_INTER;
    }

    /**
     * @param b the path angle w.r.t. robot facing angle
     * @return max speed at certain direction
     */
    public static double calcMaxSpeed(double b) {
        if ((int) b % 180 > 90) {
            b = 180 - b;
        }
        b = Math.toRadians(b);
        double x = M / (Math.tan(b) + M / N);
        double y = Math.tan(b) * x;
        return Math.sqrt(x * x + y * y);
    }
}
