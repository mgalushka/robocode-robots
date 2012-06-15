package com.maximgalushka.robocode;


import robocode.Robot;
import robocode.ScannedRobotEvent;

/**
 * <p></p>
 *
 * @author Maxim Galushka
 * @since 14.06.12
 */
public class TestRobot extends Robot {

    @Override
    public void run() {
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);
        while (true) {
            turnRadarRight(360);
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {

        double d = normalizeBearing(getHeading() - getGunHeading() + e.getBearing());
        System.out.printf(String.format("Enemy Bearing: [%f], My Heading: [%f], " +
                                        "My Gun Heading: [%f], projected turn: [%f]\n",
                                        e.getBearing(), getHeading(), getGunHeading(), d));

        turnGunRight(d);
        fire(Math.min(400 / e.getDistance(), 3));

        System.out.printf(String.format("Heat: [%s]\n", getGunHeat()));


    }

    // normalizes a bearing to between +180 and -180
    double normalizeBearing(double angle) {
        while (angle >  180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }
}
