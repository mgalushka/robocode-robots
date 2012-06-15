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
}
