/**
 * 
 */
package pt.lsts.imc4j.util;

/**
 * @author pdias
 *
 */
public class AngleUtils {
    /** 2Pi constant */
    public static final double TWO_PI_RADS = Math.PI * 2.0;

    /** To avoid initialization */
    private AngleUtils() {
    }

    /**
     * Calculates the angle between two 2D points.
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static double calcAngle(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double angle = 0.0d;

        // Calculate angle
        if (dx == 0.0) {
            if (dy == 0.0)
                angle = 0.0;
            else if (dy > 0.0)
                angle = Math.PI / 2.0;
            else
                angle = Math.PI * 3.0 / 2.0;
        }
        else if (dy == 0.0) {
            if (dx > 0.0)
                angle = 0.0;
            else
                angle = Math.PI;
        }
        else {
            if (dx < 0.0)
                angle = Math.atan(dy / dx) + Math.PI;
            else if (dy < 0.0)
                angle = Math.atan(dy / dx) + TWO_PI_RADS;
            else
                angle = Math.atan(dy / dx);
        }

        // Return
        return -1 * (angle - (Math.PI / 2.0));
    }

    /**
     * Normalizes an angle in radians.
     * 
     * @param angle The angle to normalize
     * @return The angle between 0 and 2pi
     */
    public static double nomalizeAngleRads2Pi(double angle) {
        double ret = angle;
        ret = ret % TWO_PI_RADS;
        if (ret < 0.0)
            ret += TWO_PI_RADS;
        return ret;
    }

    /**
     * Normalizes an angle in radians.
     * 
     * @param angle The angle to normalize
     * @return The angle between -pi and pi
     */
    public static double nomalizeAngleRadsPi(double angle) {
        double ret = angle;
        while (ret > Math.PI)
            ret -= TWO_PI_RADS;
        while (ret < -Math.PI)
            ret += TWO_PI_RADS;
        return ret;
    }
    
    /** 
     * Normalizes an angle in degrees.
     * 
     * @param angle The angle to normalize
     * @return The angle between 0 and 360
     */
    public static double nomalizeAngleDegrees360(double angle) {
        double ret = angle;
        ret = ret % 360.0;
        if(ret < 0.0)
            ret+= 360.0;
        return ret;
    }

    /**
     * Normalizes an angle in degrees.
     * 
     * @param angle The angle to normalize
     * @return The angle between -180 and 180
     */
    public static double nomalizeAngleDegrees180(double angle) {
        double ret = angle;
        while (ret > 180)
            ret -= 360;
        while (ret < -180)
            ret += 360;
        return ret;
    }

    /**
     * XY Coordinate conversion considering a rotation angle.
     *
     * @param angleRadians Angle
     * @param x original x value on entry, rotated x value on exit.
     * @param y original y value on entry, rotated y value on exit.
     * @param clockwiseRotation ClockwiseRotation rotation or not
     */
    public static double[] rotate(double angleRadians, double x, double y, boolean clockwiseRotation) {
        double sina = Math.sin(angleRadians), cosa = Math.cos(angleRadians);
        double[] xy = { 0, 0 };
        if (clockwiseRotation) {
            xy[0] = x * cosa + y * sina;
            xy[1] = -x * sina + y * cosa;
        }
        else {
            xy[0] = x * cosa - y * sina;
            xy[1] = x * sina + y * cosa;
        }
        return xy;
    }

    /**
     * XY Coordinate conversion considering a rotation angle.
     *
     * @param angleRadians Angle
     * @param x original x value on entry, rotated x value on exit.
     * @param y original y value on entry, rotated y value on exit.
     * @param clockwiseRotation ClockwiseRotation rotation or not
     * @param xPivot pivot x for rotation.
     * @param yPivot pivot y for rotation.
     */
    public static double[] rotate(double angleRadians, double x, double y, boolean clockwiseRotation,
            double xPivot, double yPivot) {
        double nx = x - xPivot;
        double ny = y - yPivot;
        double[] xy = rotate(angleRadians, nx, ny, clockwiseRotation);
        xy[0] += xPivot;
        xy[1] += yPivot;
        return xy;
    }
}
