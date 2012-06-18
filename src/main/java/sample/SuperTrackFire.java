package sample;

import robocode.util.*;
import robocode.*;
import java.awt.*;


/**
 * TrackFire - a Super Sample Bot by CrazyBassoonist, based on TrackFire Mathew Nelson and maintained by Flemming N. Larsen
 * <p/>
 *Random oscillating movement with slightly random Head-On Targeting
 */
public class SuperTrackFire extends AdvancedRobot {
    int dir=1;//Which way we want to move
    /**
     * TrackFire's run method
     */
    public void run() {
        // Set colors
        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);
        setBodyColor(Color.pink);
        setGunColor(Color.pink);
        setRadarColor(Color.pink);
        setScanColor(Color.pink);
        setBulletColor(Color.pink);
        turnRadarRightRadians(Double.POSITIVE_INFINITY);//turnRadarRight
    }

    /**
     * onScannedRobot:  We have a target.  Go get it.
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        double absoluteBearing = getHeadingRadians() + e.getBearingRadians();//robot's absolute bearing
        double randomGuessFactor = (Math.random() - .5) * 2;
        double maxEscapeAngle = Math.asin(8.0/(20 - (3 *Math.min(3,getEnergy()/10))));//farthest the enemy can move in the amount of time it would take for a bullet to reach them
        double randomAngle = randomGuessFactor * maxEscapeAngle;//random firing angle
        double firingAngle = Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians()+randomAngle/3);//amount to turn our gun
        setTurnLeftRadians(-90-e.getBearingRadians()*dir);//Turn perpendicular to them
        setTurnGunRightRadians(firingAngle);//Aim!
        setAhead(100*Math.random()*dir);
        setFire(getEnergy()/10);//Fire, using less energy if we have low energy
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing-getRadarHeadingRadians()));//lock on the radar
        if(Math.random()>.9){
            dir=-dir;//randomly changing move and turn direction
        }
    }
    public void onHitWall(HitWallEvent e){
        dir=-dir;//change direction when we hit the wall
    }
}