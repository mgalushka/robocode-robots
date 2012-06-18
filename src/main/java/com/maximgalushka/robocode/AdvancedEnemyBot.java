package com.maximgalushka.robocode;

import robocode.Robot;
import robocode.ScannedRobotEvent;

/**
 * <p></p>
 *
 * @author Maxim Galushka
 * @since 15.06.12
 */
public class AdvancedEnemyBot extends EnemyBot{

    private double x;
    private double y;

    @Override
    public void reset() {
        super.reset();

        x = 0.0;
        y = 0.0;
    }

    public void update(ScannedRobotEvent e, Robot robot) {
        super.update(e);

        double absBearingDeg = (robot.getHeading() + e.getBearing());
        if (absBearingDeg < 0) absBearingDeg += 360;

        // yes, you use the _sine_ to get the X value because 0 deg is North
        x = robot.getX() + Math.sin(Math.toRadians(absBearingDeg)) * e.getDistance();

        // yes, you use the _cosine_ to get the Y value because 0 deg is North
        y = robot.getY() + Math.cos(Math.toRadians(absBearingDeg)) * e.getDistance();
    }

    public double getFutureX(long when){
        return x + Math.sin(Math.toRadians(getHeading())) * getVelocity() * when;
    }

    public double getFutureY(long when){
        return y + Math.cos(Math.toRadians(getHeading())) * getVelocity() * when;
    }

    public double getFutureT(Robot robot, double bulletVelocity){

        // enemy velocity
        double v_E = getVelocity();

        // temp variables
        double x_diff = x - robot.getX();
        double y_diff = y - robot.getY();

        // angles of enemy's heading
        double sin = Math.sin(Math.toRadians(getHeading()));
        double cos = Math.cos(Math.toRadians(getHeading()));

        // calculated time
        double T;
        double v_B = bulletVelocity;

        double xy = (x_diff*sin + y_diff*cos);

        T = ( (v_E*xy) + Math.sqrt(sqr(v_E)*sqr(xy) + (sqr(x_diff) + sqr(y_diff))*(sqr(v_B) + sqr(v_E))) ) / (sqr(v_B) - sqr(v_E));

        return T;

    }

    private static double sqr(double in){
        return in * in;
    }
}
