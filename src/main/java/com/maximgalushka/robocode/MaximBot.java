package com.maximgalushka.robocode;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

import java.awt.*;

/**
 * <p></p>
 *
 * @author Maxim Galushka
 * @since 15.06.12
 */
public class MaximBot extends AdvancedRobot {

    private EnemyBot enemy = new EnemyBot();
    private volatile byte radarDirection = 1;

    @Override
    public void run() {

        // Set colors
        setBodyColor(new Color(24, 200, 12));
        setGunColor(new Color(150, 13, 13));
        setRadarColor(new Color(255, 253, 199));
        setBulletColor(new Color(255, 127, 0));
        setScanColor(new Color(226, 250, 255));

        // custom adjust
        //setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        //setAdjustRadarForRobotTurn(true);

        enemy.reset();

        while (true) {

            doScanner();
            doMovement();
            doGun();
            execute(); // you must call this!!!

        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        if (
            // we have no enemy, or...
                enemy.none() ||
                        // the one we just spotted is significantly closer, or...
                        e.getDistance() < enemy.getDistance() - 70 ||
                        // we found the one we've been tracking
                        e.getName().equals(enemy.getName())
                ) {
            // track him
            enemy.update(e);
        }
    }

    void doScanner() {
        if (enemy.none()) {
            // look around
            setTurnRadarRight(36000);
        } else {
            // keep him inside a cone
            double turn = getHeading() - getRadarHeading() + enemy.getBearing();
            turn += 10 * radarDirection;
            setTurnRadarRight(turn);
            radarDirection *= -1;
        }
    }

    void doMovement() {
        setTurnRight(enemy.getBearing());
        // move a little closer
        if (enemy.getDistance() > 200)
            setAhead(enemy.getDistance() / 2);
        // but not too close
        if (enemy.getDistance() < 100)
            setBack(enemy.getDistance());
    }

    void doGun() {

        // don't fire if there's no enemy
        if (enemy.none()) return;

        // convenience variable
        double max = Math.max(getBattleFieldHeight(), getBattleFieldWidth());

        // only shoot if we're (close to) pointing at our enemy
        if (Math.abs(getTurnRemaining()) < 10) {
            if (enemy.getDistance() < max / 3) {
                // fire hard when close
                setFire(3);
            } else {
                // otherwise, just plink him
                setFire(1);
            }
        }
    }

    public void onScannedRobot1(ScannedRobotEvent e) {

        // if we have no enemy or we found the one we're tracking..
        if (enemy.none() || e.getName().equals(enemy.getName())) {
            // track him!
            enemy.update(e);
        }

        double d = normalizeBearing(getHeading() - getGunHeading() + e.getBearing());
        System.out.printf(String.format("Enemy Bearing: [%f], My Heading: [%f], " +
                "My Gun Heading: [%f], projected turn: [%f]\n",
                e.getBearing(), getHeading(), getGunHeading(), d));

        setTurnGunRight(d);
        setFire(Math.min(400 / e.getDistance(), 3));
        execute();

        //System.out.printf(String.format("Heat: [%s]\n", getGunHeat()));


    }

    public void onRobotDeath(RobotDeathEvent e) {
        // if the bot we were tracking died..
        if (e.getName().equals(enemy.getName())) {
            // clear his info, so we can track another
            enemy.reset();
        }
    }

    // normalizes a bearing to between +180 and -180
    double normalizeBearing(double angle) {
        while (angle >  180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }
}
