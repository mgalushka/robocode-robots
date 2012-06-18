package sample;

import robocode.*;
import robocode.util.Utils;
import java.awt.*;
import java.awt.geom.*;

/**
 * SuperCrazy - a sample robot by Chase
 * <p/>
 * This robot moves around in a crazy pattern
 * and it fires randomly within the escape angle of the enemy
 */
public class SuperCrazy extends AdvancedRobot {
    /* How many times we have decided to not change direction. */
    public int sameDirectionCounter = 0;

    /* How long we should continue to move in the current direction */
    public long moveTime = 1;

    /* The direction we are moving in */
    public static int moveDirection = 1;

    /* The speed of the last bullet that hit us, used in determining how far to move before deciding to change direction again. */
    public static double lastBulletSpeed = 15.0;

    public double wallStick = 120;

    /**
     * run: SuperCrazy's main run function
     */
    public void run() {
        /* Set some crazy colors! */
        setBodyColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
        setGunColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
        setRadarColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
        setBulletColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
        setScanColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        /* Loop forever */

        /* Simple Radar Code */
        while (true) {
            if (getRadarTurnRemaining() == 0.0)
                setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            execute();
        }
    }

    /**
     * onScannedRobot:  Fire!
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        /* For effect only, doing this every turn could cause seizures. This makes it change every 32 turns. */
        if(e.getTime() % 32 == 0) {
            /* Set some crazy colors! */
            setBodyColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
            setGunColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
            setRadarColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
            setBulletColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
            setScanColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));

            /* Change the wall stick distance, to make us even more unpredictable */
            wallStick = 120 + Math.random()*40;
        }


        double absBearing = e.getBearingRadians() + getHeadingRadians();
        double distance = e.getDistance() + (Math.random()-0.5)*5.0;

        /* Radar Turn */
        double radarTurn = Utils.normalRelativeAngle(absBearing
                // Subtract current radar heading to get turn required
                - getRadarHeadingRadians() );

        double baseScanSpan = (18.0 + 36.0*Math.random());
        // Distance we want to scan from middle of enemy to either side
        double extraTurn = Math.min(Math.atan(baseScanSpan / distance), Math.PI/4.0);
        setTurnRadarRightRadians(radarTurn + (radarTurn < 0 ? -extraTurn : extraTurn));

        /* Movement */
        if(--moveTime <= 0) {
            distance = Math.max(distance, 100 + Math.random()*50) * 1.25;
            moveTime = 50 + (long)(distance / lastBulletSpeed);

            ++sameDirectionCounter;
            if(Math.random() < 0.5 || sameDirectionCounter > 16) {
                moveDirection = -moveDirection;
                sameDirectionCounter = 0;
            }
        }


        /* Move perpendicular to our enemy, based on our movement direction */
        double goalDirection = absBearing-Math.PI/2.0*moveDirection;

        /* This is too clean for crazy! Add some real randomness. */
        goalDirection += (Math.random()-0.5) * (Math.random()*2.0 + 1.0);

        /* Smooth around the walls, if we smooth too much, reverse direction! */
        double x = getX();
        double y = getY();
        double smooth = 0;

        Rectangle2D fieldRect = new Rectangle2D.Double(18, 18, getBattleFieldWidth()-36, getBattleFieldHeight()-36);
        while (!fieldRect.contains(x+Math.sin(goalDirection)*wallStick, y+ Math.cos(goalDirection)*wallStick)) {
            /* turn a little toward enemy and try again */
            goalDirection += moveDirection*0.1;
            smooth += 0.1;
        }

        /* If we smoothed to much, then reverse direction. */
        if(smooth > 0.5 + Math.random()*0.125) {
            moveDirection = -moveDirection;
            sameDirectionCounter = 0;
        }

        double turn = Utils.normalRelativeAngle(goalDirection - getHeadingRadians());

        /* Adjust so we drive backwards if the turn is less to go backwards */
        if (Math.abs(turn) > Math.PI/2) {
            turn = Utils.normalRelativeAngle(turn + Math.PI);
            setBack(100);
        } else {
            setAhead(100);
        }

        setTurnRightRadians(turn);


        /* Gun */
        double bulletPower = 1.0 + Math.random()*2.0;
        double bulletSpeed = 20 - 3 * bulletPower;

        /* Aim at a random offset in the general direction the enemy is heading. */
        double enemyLatVel = e.getVelocity()*Math.sin(e.getHeadingRadians() - absBearing);
        double escapeAngle = Math.asin(8.0 / bulletSpeed);

        /* Signum produces 0 if it is not moving, meaning we will fire directly head on at an unmoving target */
        double enemyDirection = Math.signum(enemyLatVel);
        double angleOffset = escapeAngle * enemyDirection * Math.random();
        setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing + angleOffset - getGunHeadingRadians()));

        /* Adding this if so it does not kill itself by firing. */
        if(getEnergy() > bulletPower) {
            setFire(bulletPower);
        }
    }


    public void onHitByBullet(HitByBulletEvent e) {
        lastBulletSpeed = e.getVelocity();
    }
}