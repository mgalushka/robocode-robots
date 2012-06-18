package com.maximgalushka.robocode;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>This is solely 1-to-1 bot based on Improved Oscillator</p>
 *
 * @author Maxim Galushka
 * @since 15.06.12
 */
public class MaximBot extends AdvancedRobot {

    private AdvancedEnemyBot enemy = new AdvancedEnemyBot();
    private volatile byte radarDirection = 1;

    private AtomicInteger moveCount = new AtomicInteger(0);

    @Override
    public void run() {

        // Set colors
        setBodyColor(new Color(24, 200, 12));
        setGunColor(new Color(150, 13, 13));
        setRadarColor(new Color(255, 253, 199));
        setBulletColor(new Color(255, 127, 0));
        setScanColor(new Color(226, 250, 255));

        // divorce radar movement from gun movement
        setAdjustRadarForGunTurn(true);
        // divorce gun movement from tank movement
        setAdjustGunForRobotTurn(true);

        enemy.reset();

        setTurnRadarRight(360);

        while (true) {

            doScanner();
            doMovement();
            doGun();
            execute(); // 1 tick

        }
    }

    void doScanner() {
        if (enemy.none()) {
            // look around
            setTurnRadarRight(36000);
        } else {
            if(moveCount.get() < 100){
                // keep him inside a cone
                double turn = getHeading() - getRadarHeading() + enemy.getBearing();
                turn += 30 * radarDirection;
                setTurnRadarRight(turn);
                radarDirection *= -1;
            }
            else{
                moveCount.set(0);
            }
        }
    }

    void doMovement() {
        if(enemy.none()) return;

        setTurnRight(enemy.getBearing());
        // move a little closer
        if (enemy.getDistance() > 200)
            setAhead(enemy.getDistance() / 2);
        // but not too close
        if (enemy.getDistance() < 100)
            setBack(enemy.getDistance());
    }

    void doGun() {
        // don't shoot if I've got no enemy
        if (enemy.none())
            return;

        // calculate firepower based on distance
        double firePower = Math.min(500 / enemy.getDistance(), 3);
        // calculate speed of bullet
        double bulletSpeed = 20 - firePower * 3;
        // distance = rate * time, solved for time
        long time = (long)(enemy.getDistance() / bulletSpeed);

        long futureT = (long) Math.floor(enemy.getFutureT(this, bulletSpeed));

        System.out.printf(String.format("Time: [%d], FutureTime: [%d], Diff: [%d]\n",
                                        time, futureT, Math.abs(time - futureT)));

        // calculate gun turn to predicted x,y location
        double futureX = enemy.getFutureX(futureT);
        double futureY = enemy.getFutureY(futureT);
        double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);
        // non-predictive firing can be done like this:
        //double absDeg = absoluteBearing(getX(), getY(), enemy.getX(), enemy.getY());

        // turn the gun to the predicted x,y location
        setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));

        // if the gun is cool and we're pointed in the right direction, shoot!
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
            setFire(firePower);
        }
    }

    void doGun000() {

        // don't fire if there's no enemy
        if (enemy.none()) return;

        double d = normalizeBearing(getHeading() - getGunHeading() + enemy.getBearing());
        setTurnGunRight(d);

        // calculate firepower based on distance
        double firePower = Math.min(500 / enemy.getDistance(), 3);

        // calculate speed of bullet
        double bulletSpeed = 20 - firePower * 3;
        // distance = rate * time, solved for time
        long time = (long)(enemy.getDistance() / bulletSpeed);

        // calculate gun turn to predicted x,y location
        double futureX = enemy.getFutureX(time);
        double futureY = enemy.getFutureY(time);
        //double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);
        // non-predictive firing can be done like this:
        //double absDeg = absoluteBearing(getX(), getY(), enemy.getX(), enemy.getY());

        // turn the gun to the predicted x,y location
        //setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));

        // fire only in case if ti makes sense
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10){
            setFire(Math.min(400 / enemy.getDistance(), 3));
        }

    }

    public void onScannedRobot(ScannedRobotEvent e) {

        System.out.printf("Scanned: [%s]\n", e);

        // if we have no enemy or we found the one we're tracking..
        // 1-2-1 bot
        if (enemy.none()) {
            System.out.printf("Catched: [%s]\n", enemy);
        }

        // track him!
        enemy.update(e, this);
    }

    public void onRobotDeath(RobotDeathEvent e) {
        enemy.reset();
    }

    @Override
    public void onWin(WinEvent event) {
        for(int i=0; i<10; i++){
            ahead(5);
            back(5);
            turnLeft(5);
            turnRight(5);
        }
    }

    // computes the absolute bearing between two points
    double absoluteBearing(double x1, double y1, double x2, double y2) {
        double xo = x2-x1;
        double yo = y2-y1;
        double hyp = Point2D.distance(x1, y1, x2, y2);
        double arcSin = Math.toDegrees(Math.asin(xo / hyp));
        double bearing = 0;

        if (xo > 0 && yo > 0) { // both pos: lower-Left
            bearing = arcSin;
        } else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
            bearing = 360 + arcSin; // arcsin is negative here, actually 360 - ang
        } else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
            bearing = 180 - arcSin;
        } else if (xo < 0 && yo < 0) { // both neg: upper-right
            bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
        }

        return bearing;
    }

    // normalizes a bearing to between +180 and -180
    double normalizeBearing(double angle) {
        while (angle >  180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    @Override
    public void execute(){
        moveCount.incrementAndGet();
        super.execute();
    }
}
