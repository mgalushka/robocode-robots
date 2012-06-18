package sample;

import robocode.*;
import robocode.util.*;
import java.awt.*;

/**
 * SuperWalls - a sample robot by CrazyBassoonist based on the sample robot Walls by Mathew Nelson and maintained by Flemming N. Larsen
 * Moves around the outer edge with two targeting systems
 */
public class SuperWalls extends AdvancedRobot {
    static int HGShots;     //Number of shots with Head-On Targeting
    static int LGShots;     //Number of shots with Linear Targeting
    static int HGHits;      //Number of hits with Head-On Targeting
    static int LGHits;      //Number of hits with Linear Targeting
    boolean gunIdent;       //Used to tell which gun we are using
    int dir = 1;
    double energy;
    static int enemyFireCount = 0;

    public void run() {
        setAdjustRadarForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setBodyColor(Color.black);
        setGunColor(Color.black);
        setRadarColor(Color.orange);
        setBulletColor(Color.cyan);
        setScanColor(Color.cyan);

        setTurnRadarRight(Double.POSITIVE_INFINITY);

        // turnLeft to face a wall.
        // getHeading() % 90 means the remainder of
        // getHeading() divided by 90.
        turnLeft(getHeading() % 90);

        while (true) {
            //if (getDistanceRemaining() == 0) {
            //	turnRight(90 * dir);
            if (Utils.isNear(getHeadingRadians(), 0D) || Utils.isNear(getHeadingRadians(), Math.PI)) {
                ahead((Math.max(getBattleFieldHeight() - getY(), getY()) - 28) * dir);
            } else {
                ahead((Math.max(getBattleFieldWidth() - getX(), getX()) - 28) * dir);
            }
            turnRight(90 * dir);
            //}
        }
    }

    /**
     * onScannedRobot: Fire!
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        double absBearing = e.getBearingRadians() + getHeadingRadians();                // The enemies location relative to us
        double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing); // The enemies lateral velocity
        double radarTurn = absBearing - getRadarHeadingRadians();                       // The amount to turn our radar

        double HGRating = (double) HGHits / HGShots;
        double LGRating = (double) LGHits / LGShots;

        if (energy > (energy = e.getEnergy())) {
            enemyFireCount++;
            if (enemyFireCount % 5 == 0) {
                dir = -dir;
                if (Utils.isNear(getHeadingRadians(), 0D) || Utils.isNear(getHeadingRadians(), Math.PI)) {
                    setAhead((Math.max(getBattleFieldHeight() - getY(), getY()) - 28) * dir);
                } else {
                    setAhead((Math.max(getBattleFieldWidth() - getX(), getX()) - 28) * dir);
                }
            }
        }

        setMaxVelocity(Math.random() * 12);

        if ((getRoundNum() == 0 || LGRating > HGRating) && getRoundNum() != 1){ // In the first round or when linear gun got more hitting percentage use linear targeting
            double bulletPower = Math.min(3, e.getEnergy() / 4);
            setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + Math.asin(latVel / (20 - 3 * bulletPower))));
            LGShots++;
            gunIdent = true;
            setFire(bulletPower); // Fire the minimum amount of energy needed to finish off the other robot
        } else { // in second round or when the head-on gun got more hitting percentage, use head-on gun.
            setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians()));
            HGShots++;
            gunIdent = false;
            setFire(e.getEnergy() / 4); // Fire the minimum amount of energy needed to finish off the other robot
        }
        setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn) * 2); // Make the radar lock on
    }

    public void onBulletHit(BulletHitEvent e) {
        if(gunIdent) {
            LGHits = LGHits+1;
        } else {
            HGHits = HGHits+1;
        }
    }
}